package io.github.thingersoft.pm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This class acts as a container agnostic single source of truth for configuring applications through properties files.<br>
 * Applications will likely call {@link PropertiesInjector#loadProperties(Boolean, String...)} at startup.
 */
public class PropertiesInjector {

	private PropertiesInjector() {
	}

	private static ApplicationProperties applicationProperties = new ApplicationProperties();
	private static Map<String, Thread> watcherThreads = new HashMap<>();

	/**
	 * @see PropertiesInjector#loadProperties(Boolean, String...)
	 */
	public synchronized static void loadProperties(String... propertiesLocations) {
		loadProperties(true, propertiesLocations);
	}

	/**
	 * Load properties from the provided locations and merges them into the centralized storage.<br>
	 * When {@code hotReload} is {@code true} a new watcher thread will be spawned for each location provided.<br>
	 * In this case the caller application must invoke {@link PropertiesInjector#stopWatching()} before shutting down.
	 * 
	 * @param hotReload
	 * changes monitoring flag - defaults to {@code true}
	 * 
	 * @param propertiesLocations
	 * file system locations of properties
	 */
	public synchronized static void loadProperties(Boolean hotReload, String... propertiesLocations) {
		for (final String propertiesLocation : propertiesLocations) {
			storeProperties(propertiesLocation);
			if (hotReload == null || hotReload) {
				Thread watcherThread = new Thread(new Runnable() {

					private long lastModified = -1;

					@Override
					public void run() {
						Path propertiesPath = FileSystems.getDefault().getPath(propertiesLocation);
						Path propertiesDirectory = propertiesPath.getParent();
						try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
							propertiesDirectory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
							while (true) {
								final WatchKey wk = watchService.take();
								for (WatchEvent<?> event : wk.pollEvents()) {
									final Path changed = (Path) event.context();
									if (changed.equals(propertiesPath.getFileName())
											&& propertiesDirectory.resolve(changed).toFile().lastModified() != lastModified) {
										lastModified = propertiesDirectory.resolve(changed).toFile().lastModified();
										storeProperties(propertiesLocation);
									}
								}
								boolean valid = wk.reset();
								if (!valid) {
									throw new RuntimeException("Can't resume properties file monitoring: the watch key is no longer valid");
								}
							}
						} catch (IOException e) {
							throw new RuntimeException("Can't start monitoring properties file", e);
						} catch (InterruptedException | ClosedWatchServiceException e) {
							// JUST STOPPED WATCHING
						}
					}
				});

				watcherThreads.put(propertiesLocation, watcherThread);

				watcherThread.start();
			}
		}
	}

	/**
	 * Stops all threads watching for file changes. 
	 */
	public synchronized static void stopWatching() {
		for (Thread watcherThread : watcherThreads.values()) {
			watcherThread.interrupt();
		}
		watcherThreads = new HashMap<>();
	}

	/**
	 * Resets {@link PropertiesInjector} to its initial state. 
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
		} catch (IOException | NullPointerException e) {
			throw new RuntimeException("Can't load properties file", e);
		}
	}

}
