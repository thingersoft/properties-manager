package io.github.thingersoft.pm;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a container agnostic single source of truth for configuring applications through properties files.<br>
 * Applications will likely call {@link PropertiesInjector#loadProperties(Boolean, String...)} at startup.
 */
public class PropertiesInjector {

	private PropertiesInjector() {
	}

	private static final Logger LOG = LoggerFactory.getLogger(PropertiesInjector.class);

	private static ApplicationProperties applicationProperties = new ApplicationProperties();
	private static Map<String, FileAlterationMonitor> monitors = new HashMap<>();

	private static final long POLL_INTERVAL = 1000;

	private static String datePattern = new SimpleDateFormat().toPattern();
	private static Locale locale = Locale.getDefault();

	/**
	 * @see PropertiesInjector#loadProperties(Boolean, String...)
	 */
	public synchronized static void loadProperties(String... propertiesLocations) {
		loadProperties(true, propertiesLocations);
	}

	/**
	 * Load properties from the provided locations and merges them into the centralized storage.<br>
	 * When {@code hotReload} is {@code true} a new monitor thread will be spawned for each location provided.<br>
	 * In this case the caller application must invoke {@link PropertiesInjector#stopWatching()} before shutting down.
	 * 
	 * @param hotReload
	 * changes monitoring flag - defaults to {@code true}
	 * 
	 * @param propertiesLocations
	 * file system locations of properties
	 * @throws Exception 
	 */
	public synchronized static void loadProperties(Boolean hotReload, String... propertiesLocations) {
		for (final String propertiesLocation : propertiesLocations) {
			storeProperties(propertiesLocation);
			if (hotReload == null || hotReload) {

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
						storeProperties(propertiesLocation);
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
	 * Resets {@link PropertiesInjector} to its initial state. 
	 * @throws Exception 
	 */
	public synchronized static void reset() {
		stopWatching();
		applicationProperties = new ApplicationProperties();
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
	 * PropertiesInjector#setDatePattern(String)
	 * @see
	 * PropertiesInjector#setLocale(Locale)
	 */
	public static Date getDate(String key) throws IllegalArgumentException {
		try {
			return new SimpleDateFormat(datePattern, locale).parse(getProperty(key));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Can't parse date property", e);
		}
	}

	/**
	 * @return
	 * the current set of properties
	 */
	public static ApplicationProperties getProperties() {
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
	public static ApplicationProperties getProperties(String keyPattern) {
		ApplicationProperties filteredProperties = new ApplicationProperties();
		for (Entry<Object, Object> property : applicationProperties.entrySet()) {
			if (Pattern.matches(keyPattern, (CharSequence) property.getKey())) {
				filteredProperties.put(property.getKey(), property.getValue());
			}
		}
		return filteredProperties;
	}

	public static void setObfuscatedPropertyPattern(String obfuscatedPropertyPattern) {
		applicationProperties.setObfuscatedPropertyPattern(obfuscatedPropertyPattern);
	}

	public static void setObfuscatedPropertyPlaceholder(String obfuscatedPropertyPlaceholder) {
		applicationProperties.setObfuscatedPropertyPlaceholder(obfuscatedPropertyPlaceholder);
	}

	private synchronized static void storeProperties(String propertiesLocation) {
		try (FileInputStream fis = new FileInputStream(new File(propertiesLocation))) {
			Properties propertiesToLoad = new Properties();
			propertiesToLoad.load(fis);
			applicationProperties.putAll(propertiesToLoad);
			LOG.info("Properties updated. Current entries: {}", applicationProperties);
		} catch (IOException | NullPointerException e) {
			throw new RuntimeException("Can't load properties file", e);
		}
	}

	public static long getPollInterval() {
		return POLL_INTERVAL;
	}

	public static String getDatePattern() {
		return datePattern;
	}

	public static void setDatePattern(String datePattern) {
		PropertiesInjector.datePattern = datePattern;
	}

	public static Locale getLocale() {
		return locale;
	}

	public static void setLocale(Locale locale) {
		PropertiesInjector.locale = locale;
	}

}
