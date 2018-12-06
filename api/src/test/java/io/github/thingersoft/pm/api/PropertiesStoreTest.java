package io.github.thingersoft.pm.api;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.thingersoft.pm.test.PropertiesTest;

public class PropertiesStoreTest {

	@BeforeClass
	public static void init() throws IOException {
		PropertiesTest.initProperties();
	}

	@After
	public void reset() {
		PropertiesStore.reset();
	}

	@Test
	public void load() {
		Entry<String, Properties> typedPropertiesFileEntry = PropertiesTest.getTypedPropertiesTestEntry();
		PropertiesStore.loadProperties(false, typedPropertiesFileEntry.getKey());
		checkProperties(typedPropertiesFileEntry);
	}

	@Test
	public void loadMultiple() {
		for (String tempFileLocation : PropertiesTest.PROPERTIES_FILES_MAP.keySet()) {
			PropertiesStore.loadProperties(false, tempFileLocation);
		}
		for (Entry<String, Properties> propertyFileEntry : PropertiesTest.PROPERTIES_FILES_MAP.entrySet()) {
			checkProperties(propertyFileEntry);
		}
	}

	@Test
	public void hotReload() throws FileNotFoundException, IOException, InterruptedException {
		Entry<String, Properties> typedPropertiesFileEntry = PropertiesTest.getTypedPropertiesTestEntry();
		PropertiesStore.loadProperties(true, typedPropertiesFileEntry.getKey());
		Properties propertiesToEdit = typedPropertiesFileEntry.getValue();
		propertiesToEdit.put(PropertiesTest.STRING_KEY, "edited_string_value");
		try (FileOutputStream fos = new FileOutputStream(typedPropertiesFileEntry.getKey())) {
			propertiesToEdit.store(fos, null);
		}
		Thread.sleep(PropertiesStore.getPollInterval() + 500);
		checkProperties(typedPropertiesFileEntry);
	}

	private void checkProperties(Entry<String, Properties> propertyFileEntry) {
		Properties inMemoryProperties = propertyFileEntry.getValue();
		for (Object key : inMemoryProperties.keySet()) {
			assertTrue(inMemoryProperties.get(key).equals(PropertiesStore.getProperty((String) key)));
		}
	}

}
