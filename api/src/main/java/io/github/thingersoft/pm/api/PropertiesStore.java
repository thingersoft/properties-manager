package io.github.thingersoft.pm.api;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import io.github.thingersoft.pm.api.annotations.Property;
import io.github.thingersoft.pm.api.data.PropertiesStoreOptions;
import io.github.thingersoft.pm.api.data.SupportedTypes;

/**
 * This class acts as a container agnostic single source of truth for configuring applications through properties files.<br>
 */
public final class PropertiesStore {

	private PropertiesStore() {
	}

	private static final Logger LOG = LoggerFactory.getLogger(PropertiesStore.class);

	private static Properties applicationProperties = new Properties();
	private static Map<String, FileAlterationMonitor> monitors = new HashMap<>();
	private static Map<String, Field> injectionMap = new HashMap<>();

	private static final long POLL_INTERVAL = 1000;
	private static final Pattern LOCATION_VARIABLE_PATTERN = Pattern.compile("\\{.+\\}");

	private static PropertiesStoreOptions options = new PropertiesStoreOptions();

	static {
		// look for classes annotated with @Properties
		try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
			ClassInfoList classInfoList = scanResult.getClassesWithAnnotation(io.github.thingersoft.pm.api.annotations.Properties.class.getName());
			for (ClassInfo mappedClassInfo : classInfoList) {
				Class<?> mappedClass = mappedClassInfo.loadClass();
				initByAnnotatedClass(mappedClass);
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Properties injection mapping failed", e);
		}
	}

	public static void initByAnnotatedClass(Class<?> mappedClass) {
		// look for fields annotated with @Property within matching classes
		for (Field field : mappedClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(Property.class)) {
				// save property key and field for future injection
				field.setAccessible(true);
				injectionMap.put(field.getAnnotation(Property.class).value(), field);
			}
		}

		io.github.thingersoft.pm.api.annotations.Properties propertiesAnnotation = mappedClass
				.getAnnotation(io.github.thingersoft.pm.api.annotations.Properties.class);

		// configure and initialize store by @Properties annotation attributes
		options.setHotReload(propertiesAnnotation.hotReload());
		options.setDatePattern(propertiesAnnotation.datePattern());
		options.setObfuscatedPropertyPattern(propertiesAnnotation.obfuscatedPropertyPattern());
		options.setObfuscatedPropertyPlaceholder(propertiesAnnotation.obfuscatedPropertyPlaceholder());
		options.setLocale(propertiesAnnotation.locale());
		loadProperties(propertiesAnnotation.propertiesLocations());
	}

	/**
	 * Load properties from the provided locations and merges them into the centralized storage.<br>
	 * Locations may contain system and/or environment variables within curly braces:<br><br>
	 * {myapp.conf.folder}/app.properties<br><br>
	 * If location is a folder each *.properties file inside will be loaded.<br>
	 * When {@code hotReload} is {@code true} a new monitor thread will be spawned for each scanned properties file.<br>
	 * In this case the caller application must invoke {@link PropertiesStore#stopWatching()} before shutting down.
	 * 
	 * @param propertiesLocations
	 * file system locations of properties
	 */
	public synchronized static void loadProperties(String... propertiesLocations) {
		List<String> interpolatedPropertiesLocations = new ArrayList<>();
		for (String propertiesLocation : propertiesLocations) {
			String interpolatedLocation = propertiesLocation;
			Matcher variablesMatcher = LOCATION_VARIABLE_PATTERN.matcher(propertiesLocation);
			while (variablesMatcher.find()) {
				String matchedString = variablesMatcher.group();
				String variable = matchedString.substring(1, matchedString.length() - 1);
				String variableValue = System.getProperty(variable) != null ? System.getProperty(variable) : System.getenv(variable);
				interpolatedLocation = interpolatedLocation.replace(matchedString, variableValue);
			}
			interpolatedPropertiesLocations.add(interpolatedLocation);
		}

		for (final String propertiesLocation : interpolatedPropertiesLocations) {
			final Path propertiesPath = FileSystems.getDefault().getPath(propertiesLocation);
			if (propertiesPath.toFile().isDirectory()) {
				try {
					DirectoryStream<Path> propertiesStream = Files.newDirectoryStream(propertiesPath, "*.properties");
					for (Path propertiesFilePath : propertiesStream) {
						internalLoadProperties(propertiesFilePath.toString());
					}
					propertiesStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				internalLoadProperties(propertiesLocation);
			}
		}
	}

	private static void internalLoadProperties(final String propertiesLocation) {
		updateProperties(propertiesLocation);

		// if hotReload flag is active spawn a new thread to watch for properties file changes 
		if (options.isHotReload()) {

			final Path propertiesPath = FileSystems.getDefault().getPath(propertiesLocation);
			Path propertiesDirectory = propertiesPath.getParent();

			FileAlterationObserver observer = new FileAlterationObserver(propertiesDirectory.toString(), new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return propertiesPath.equals(pathname.toPath());
				}
			});
			FileAlterationMonitor monitor = new FileAlterationMonitor(POLL_INTERVAL);
			FileAlterationListener listener = new FileAlterationListenerAdaptor() {

				@Override
				public void onFileChange(File file) {
					LOG.info("Change detected for properties file {}", propertiesLocation);
					updateProperties(propertiesLocation);
				}

				@Override
				public void onFileCreate(File file) {
				}

				@Override
				public void onFileDelete(File file) {
				}

			};
			observer.addListener(listener);
			monitor.addObserver(observer);
			try {
				monitor.start();
				monitors.put(propertiesLocation, monitor);
			} catch (Exception e) {
				throw new RuntimeException("Can't start monitoring properties " + propertiesLocation, e);
			}
		}
	}

	private synchronized static void updateProperties(String propertiesLocation) {

		// load properties file and merge entries into applicationProperties
		try (FileInputStream fis = new FileInputStream(new File(propertiesLocation))) {
			Properties propertiesToLoad = new Properties();
			propertiesToLoad.load(fis);
			applicationProperties.putAll(propertiesToLoad);
			LOG.info("Properties updated. Current entries: {}", toText());
		} catch (IOException | NullPointerException e) {
			throw new RuntimeException("Can't load properties file", e);
		}

		// perform properties injection into fields scanned at initialization stage
		for (Entry<String, Field> injectionEntry : injectionMap.entrySet()) {
			try {

				Field field = injectionEntry.getValue();
				SupportedTypes supportedType = SupportedTypes.getSupportedType(field.getType());
				if (supportedType == null) {
					throw new RuntimeException("Unsupported field type: " + field.getType());
				}

				String propertyKey = field.getAnnotation(Property.class).value();
				Object propertyValue = null;

				// perform automatic type conversion
				switch (supportedType) {
				case BIGDECIMAL:
					propertyValue = PropertiesStore.getBigDecimal(propertyKey);
					break;
				case DATE:
					propertyValue = PropertiesStore.getDate(propertyKey);
					break;
				case DOUBLE:
					propertyValue = PropertiesStore.getDouble(propertyKey);
					break;
				case FLOAT:
					propertyValue = PropertiesStore.getFloat(propertyKey);
					break;
				case INTEGER:
					propertyValue = PropertiesStore.getInteger(propertyKey);
					break;
				case LONG:
					propertyValue = PropertiesStore.getLong(propertyKey);
					break;
				case STRING:
					propertyValue = PropertiesStore.getProperty(propertyKey);
					break;
				}
				field.set(null, propertyValue);

			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * Stops all threads watching for file changes. 
	 * @throws Exception 
	 */
	public synchronized static void stopWatching() {
		for (FileAlterationMonitor monitor : monitors.values()) {
			try {
				monitor.stop();
			} catch (Exception e) {
				LOG.error("Failed stopping properties monitor " + monitor, e);
			}
		}
		if (!monitors.isEmpty()) {
			LOG.info("Properties monitoring stopped");
			monitors = new HashMap<>();
		}
	}

	/**
	 * Resets {@link PropertiesStore} to its initial state. 
	 * @throws Exception 
	 */
	public synchronized static void reset() {
		stopWatching();
		applicationProperties = new Properties();
	}

	/**
	 * Gets a single property from the centralized storage.
	 * 
	 * @param key
	 * property key
	 * 
	 * @return
	 * property value as a plain {@code String}
	 */
	public static String getProperty(String key) {
		return applicationProperties.getProperty(key);
	}

	/**
	 * Gets a single property from the centralized storage.
	 * 
	 * @param key
	 *  property key
	 *  
	 * @return
	 * property value as {@code Long}
	 * 
	 * @throws NumberFormatException
	 * if the property value does not contain a parsable {@code Long}.
	 * 
	 */
	public static Long getLong(String key) throws NumberFormatException {
		return new Long(getProperty(key));
	}

	/**
	 * Gets a single property from the centralized storage.
	 * 
	 * @param key
	 *  property key
	 *  
	 * @return
	 * property value as {@code Integer}
	 * 
	 * @throws NumberFormatException
	 * if the property value does not contain a parsable {@code Integer}.
	 * 
	 */
	public static Integer getInteger(String key) throws NumberFormatException {
		return new Integer(getProperty(key));
	}

	/**
	 * Gets a single property from the centralized storage.
	 * 
	 * @param key
	 *  property key
	 *  
	 * @return
	 * property value as {@code BigDecimal}
	 * 
	 * @throws NumberFormatException
	 * if the property value does not contain a parsable {@code BigDecimal}.
	 * 
	 */
	public static BigDecimal getBigDecimal(String key) throws NumberFormatException {
		return new BigDecimal(getProperty(key));
	}

	/**
	 * Gets a single property from the centralized storage.
	 * 
	 * @param key
	 *  property key
	 *  
	 * @return
	 * property value as {@code Float}
	 * 
	 * @throws NumberFormatException
	 * if the property value does not contain a parsable {@code Float}.
	 * 
	 */
	public static Float getFloat(String key) throws NumberFormatException {
		return new Float(getProperty(key));
	}

	/**
	 * Gets a single property from the centralized storage.
	 * 
	 * @param key
	 *  property key
	 *  
	 * @return
	 * property value as {@code Double}
	 * 
	 * @throws NumberFormatException
	 * if the property value does not contain a parsable {@code Double}.
	 * 
	 */
	public static Double getDouble(String key) throws NumberFormatException {
		return new Double(getProperty(key));
	}

	/**
	 * Gets a single property from the centralized storage.
	 * 
	 * @param key
	 *  property key
	 *  
	 * @return
	 * property value as {@code Date}
	 * 
	 * @throws IllegalArgumentException 
	 * if the property value can't be parsed as {@code Date} using current {@code datePattern} and {@code locale}.
	 * 
	 * @see 
	 * PropertiesStore#setDatePattern(String)
	 * @see
	 * PropertiesStore#setLocale(Locale)
	 */
	public static Date getDate(String key) throws IllegalArgumentException {
		try {
			return new SimpleDateFormat(options.getDatePattern(), options.getLocale()).parse(getProperty(key));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Can't parse date property", e);
		}
	}

	/**
	 * @return
	 * the current set of properties
	 */
	public static Properties getProperties() {
		return applicationProperties;
	}

	/**
	 * Returns a subset of properties whose keys match the provided regex.
	 * 
	 * @param keyPattern
	 * regular expression to be matched by properties keys
	 * 
	 * @return
	 * the matching set of properties
	 */
	public static Properties getProperties(String keyPattern) {
		Properties filteredProperties = new Properties();
		for (Entry<Object, Object> property : applicationProperties.entrySet()) {
			if (Pattern.matches(keyPattern, (CharSequence) property.getKey())) {
				filteredProperties.put(property.getKey(), property.getValue());
			}
		}
		return filteredProperties;
	}

	public static PropertiesStoreOptions getOptions() {
		return options;
	}

	public static void setOptions(PropertiesStoreOptions options) {
		PropertiesStore.options = options;
	}

	public static long getPollInterval() {
		return POLL_INTERVAL;
	}

	/**
	 * Returns a string representation of the current properties
	 * in the form of a set of entries, enclosed in braces and separated
	 * by the ASCII characters "{@code ,} " (comma and space).<br>
	 * Each entry is rendered as the key, an equals sign {@code =}, and the
	 * associated string value.<br>
	 * If the key matches the {@code config.obfuscatedPropertyPattern} its value will be replaced by the {@code config.obfuscatedPropertyPlaceholder}.<br>
	 * 
	 * @see 
	 * PropertiesStoreOptions#setObfuscatedPropertyPattern(String)
	 * @see 
	 * PropertiesStoreOptions#setObfuscatedPropertyPlaceholder(String)
	 *
	 * @return 
	 * a string representation of the current properties
	 */
	public static String toText() {
		List<String> properties = new ArrayList<>();
		for (Entry<Object, Object> property : applicationProperties.entrySet()) {
			String key = property.getKey().toString();
			String obfuscatedPropertyPattern = options.getObfuscatedPropertyPattern();
			String value = obfuscatedPropertyPattern != null && key.matches(obfuscatedPropertyPattern) ? options.getObfuscatedPropertyPlaceholder()
					: "" + property.getValue();
			properties.add(key + "=" + value);
		}
		return "{" + StringUtils.join(properties, ", ") + "}";
	}

}
