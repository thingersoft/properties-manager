package io.github.thingersoft.pm;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.thingersoft.pm.annotations.Property;

public class PropertiesInjectorTest {

	private static final String[] FORMATTING_LABELS = { "1", "2" };
	private static final Map<String, Properties> PROPERTIES_FILES_MAP = new LinkedHashMap<>();
	private static final String BASE_KEY_NAME = "key";

	private static final String ANNOTATION_KEY = BASE_KEY_NAME + "11";

	@Property(ANNOTATION_KEY)
	private String annotationTest;

	@BeforeClass
	public static void initProperties() throws IOException {
		for (String formattingFileLabel : FORMATTING_LABELS) {
			File temporaryFile = File.createTempFile("properties_injector_test" + formattingFileLabel, null);
			Properties properties = new Properties();
			for (String formattingPropertyLabel : FORMATTING_LABELS) {
				properties.setProperty(BASE_KEY_NAME + formattingFileLabel + formattingPropertyLabel, "value" + formattingPropertyLabel);
			}
			PROPERTIES_FILES_MAP.put(temporaryFile.getAbsolutePath(), properties);
			try (FileOutputStream fos = new FileOutputStream(temporaryFile)) {
				properties.store(fos, null);
			}

		}
	}

	@After
	public void reset() {
		PropertiesInjector.reset();
	}

	@Test
	public void load() {
		Entry<String, Properties> propertyFileEntry = PROPERTIES_FILES_MAP.entrySet().iterator().next();
		PropertiesInjector.loadProperties(false, propertyFileEntry.getKey());
		checkProperty(propertyFileEntry);
	}

	@Test
	public void loadMultiple() {
		for (String tempFileLocation : PROPERTIES_FILES_MAP.keySet()) {
			PropertiesInjector.loadProperties(false, tempFileLocation);
		}
		for (Entry<String, Properties> propertyFileEntry : PROPERTIES_FILES_MAP.entrySet()) {
			checkProperty(propertyFileEntry);
		}
	}

	@Test
	public void hotReload() throws FileNotFoundException, IOException, InterruptedException {
		for (String tempFileLocation : PROPERTIES_FILES_MAP.keySet()) {
			PropertiesInjector.loadProperties(true, tempFileLocation);
		}
		Entry<String, Properties> propertiesToEditFileEntry = PROPERTIES_FILES_MAP.entrySet().iterator().next();
		Properties propertiesToEdit = propertiesToEditFileEntry.getValue();
		Object keyToEdit = propertiesToEdit.keySet().iterator().next();
		String editedValue = "edited-value";
		propertiesToEdit.put(keyToEdit, editedValue);
		try (FileOutputStream fos = new FileOutputStream(propertiesToEditFileEntry.getKey())) {
			propertiesToEdit.store(fos, null);
		}
		Thread.sleep(500);
		checkProperty(propertiesToEditFileEntry);
	}

	@Test
	public void injectThroughAnnotation() {
		Entry<String, Properties> propertyFileEntry = PROPERTIES_FILES_MAP.entrySet().iterator().next();
		PropertiesInjector.loadProperties(false, propertyFileEntry.getKey());
		assertTrue(propertyFileEntry.getValue().getProperty(ANNOTATION_KEY).equals(annotationTest));
	}

	private void checkProperty(Entry<String, Properties> propertyFileEntry) {
		Properties inMemoryProperties = propertyFileEntry.getValue();
		for (Object key : inMemoryProperties.keySet()) {
			assertTrue(inMemoryProperties.get(key).equals(PropertiesInjector.getProperty((String) key)));
		}
	}

}
