package io.github.thingersoft.pm.api;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.thingersoft.pm.api.annotations.Property;
import io.github.thingersoft.pm.test.PropertiesTestUtility;

public class PropertiesStoreTest {

	@BeforeClass
	public static void init() throws IOException {
		PropertiesTestUtility.initProperties();
	}

	@After
	public void reset() {
		PropertiesStore.reset();
	}

	@Test
	public void load() {
		Entry<String, Properties> typedPropertiesFileEntry = loadTypedProperties(false);
		checkProperties(typedPropertiesFileEntry);
	}

	@Test
	public void loadWithInterpolatedPath() {
		Entry<String, Properties> typedPropertiesFileEntry = PropertiesTestUtility.getTypedPropertiesTestEntry();
		PropertiesStore.getOptions().setHotReload(false);
		String path = typedPropertiesFileEntry.getKey();

		String variableKey = "pm.test.var";
		int variableRightBound = 2;
		String pathToInterpolate = "{" + variableKey + "}" + path.substring(variableRightBound);
		String variableValue = path.substring(0, variableRightBound);
		System.setProperty(variableKey, variableValue);

		PropertiesStore.loadProperties(pathToInterpolate);
		checkProperties(typedPropertiesFileEntry);
	}

	@Test
	public void loadMultiple() {
		for (String tempFileLocation : PropertiesTestUtility.PROPERTIES_FILES_MAP.keySet()) {
			PropertiesStore.getOptions().setHotReload(false);
			PropertiesStore.loadProperties(tempFileLocation);
		}
		for (Entry<String, Properties> propertyFileEntry : PropertiesTestUtility.PROPERTIES_FILES_MAP.entrySet()) {
			checkProperties(propertyFileEntry);
		}
	}

	@Test
	public void hotReload() throws FileNotFoundException, IOException, InterruptedException {
		Entry<String, Properties> typedPropertiesFileEntry = loadTypedProperties(true);
		Properties propertiesToEdit = typedPropertiesFileEntry.getValue();
		propertiesToEdit.put(PropertiesTestUtility.STRING_KEY, "edited_string_value");
		try (FileOutputStream fos = new FileOutputStream(typedPropertiesFileEntry.getKey())) {
			propertiesToEdit.store(fos, null);
		}
		Thread.sleep(PropertiesStore.getPollInterval() + 500);
		checkProperties(typedPropertiesFileEntry);
	}

	@Test
	public void inject() {
		loadTypedProperties(false);
		assertTrue(PropertiesStore.getProperty(PropertiesTestUtility.STRING_KEY).equals(PropertiesInjectionTest.stringField));
		assertTrue(PropertiesStore.getInteger(PropertiesTestUtility.INTEGER_KEY).equals(PropertiesInjectionTest.integerField));
		assertTrue(PropertiesStore.getLong(PropertiesTestUtility.LONG_KEY).equals(PropertiesInjectionTest.longField));
		assertTrue(PropertiesStore.getFloat(PropertiesTestUtility.FLOAT_KEY).equals(PropertiesInjectionTest.floatField));
		assertTrue(PropertiesStore.getDouble(PropertiesTestUtility.DOUBLE_KEY).equals(PropertiesInjectionTest.doubleField));
		assertTrue(PropertiesStore.getBigDecimal(PropertiesTestUtility.BIGDECIMAL_KEY).equals(PropertiesInjectionTest.bigDecimalField));
		assertTrue(PropertiesStore.getDate(PropertiesTestUtility.DATE_KEY).equals(PropertiesInjectionTest.dateField));
	}

	private Entry<String, Properties> loadTypedProperties(boolean hotReload) {
		Entry<String, Properties> typedPropertiesFileEntry = PropertiesTestUtility.getTypedPropertiesTestEntry();
		PropertiesStore.getOptions().setHotReload(hotReload);
		PropertiesStore.loadProperties(typedPropertiesFileEntry.getKey());
		return typedPropertiesFileEntry;
	}

	private void checkProperties(Entry<String, Properties> propertyFileEntry) {
		Properties inMemoryProperties = propertyFileEntry.getValue();
		for (Object key : inMemoryProperties.keySet()) {
			assertTrue(inMemoryProperties.get(key).equals(PropertiesStore.getProperty((String) key)));
		}
	}

	@io.github.thingersoft.pm.api.annotations.Properties
	public static class PropertiesInjectionTest {

		@Property(PropertiesTestUtility.STRING_KEY)
		public static String stringField;

		@Property(PropertiesTestUtility.INTEGER_KEY)
		public static Integer integerField;

		@Property(PropertiesTestUtility.LONG_KEY)
		public static Long longField;

		@Property(PropertiesTestUtility.FLOAT_KEY)
		public static Float floatField;

		@Property(PropertiesTestUtility.DOUBLE_KEY)
		public static Double doubleField;

		@Property(PropertiesTestUtility.BIGDECIMAL_KEY)
		public static BigDecimal bigDecimalField;

		@Property(PropertiesTestUtility.DATE_KEY)
		public static Date dateField;
	}

}
