package io.github.thingersoft.pm;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.thingersoft.pm.annotations.Property;

public class PropertiesInjectorTest {

	private static final String STRING_PROPERTIES_LABEL = "string_properties_file";
	private static final String TYPED_PROPERTIES_LABEL = "typed_properties_file";

	private static final String DATE_KEY = "date_key";
	private static final String DOUBLE_KEY = "double_key";
	private static final String FLOAT_KEY = "float_key";
	private static final String BIGDECIMAL_KEY = "bigdecimal_key";
	private static final String LONG_KEY = "long_key";
	private static final String INTEGER_KEY = "integer_key";
	private static final String STRING_KEY = "string_key";

	private static final Map<String, Properties> PROPERTIES_FILES_MAP = new LinkedHashMap<>();

	@Property(STRING_KEY)
	private String annotatedString;
	@Property(INTEGER_KEY)
	private Integer annotatedInteger;
	@Property(LONG_KEY)
	private Long annotatedLong;
	@Property(BIGDECIMAL_KEY)
	private BigDecimal annotatedBigDecimal;
	@Property(FLOAT_KEY)
	private Float annotatedFloat;
	@Property(DOUBLE_KEY)
	private Double annotatedDouble;
	@Property(DATE_KEY)
	private Date annotatedDate;

	@BeforeClass
	public static void initProperties() throws IOException {
		Properties typedProperties = new Properties();
		typedProperties.put(STRING_KEY, "string_value");
		typedProperties.put(INTEGER_KEY, "1");
		typedProperties.put(LONG_KEY, "2");
		typedProperties.put(BIGDECIMAL_KEY, "3.0");
		typedProperties.put(FLOAT_KEY, "4.0");
		typedProperties.put(DOUBLE_KEY, "5.0");
		typedProperties.put(DATE_KEY, new SimpleDateFormat().format(new Date()));

		File temporaryTypedFile = File.createTempFile(TYPED_PROPERTIES_LABEL, null);
		PROPERTIES_FILES_MAP.put(temporaryTypedFile.getAbsolutePath(), typedProperties);
		try (FileOutputStream fos = new FileOutputStream(temporaryTypedFile)) {
			typedProperties.store(fos, null);
		}

		Properties stringProperties = new Properties();
		stringProperties.put("string_key_0", "string_value_0");
		File temporaryStringFile = File.createTempFile(STRING_PROPERTIES_LABEL, null);
		PROPERTIES_FILES_MAP.put(temporaryStringFile.getAbsolutePath(), stringProperties);
		try (FileOutputStream fos = new FileOutputStream(temporaryStringFile)) {
			stringProperties.store(fos, null);
		}
	}

	@After
	public void reset() {
		PropertiesInjector.reset();
	}

	@Test
	public void load() {
		Entry<String, Properties> typedPropertiesFileEntry = loadTypedPropertiesGetTestEntry(false);
		checkProperties(typedPropertiesFileEntry);
	}

	@Test
	public void loadMultiple() {
		for (String tempFileLocation : PROPERTIES_FILES_MAP.keySet()) {
			PropertiesInjector.loadProperties(false, tempFileLocation);
		}
		for (Entry<String, Properties> propertyFileEntry : PROPERTIES_FILES_MAP.entrySet()) {
			checkProperties(propertyFileEntry);
		}
	}

	@Test
	public void hotReload() throws FileNotFoundException, IOException, InterruptedException {
		Entry<String, Properties> typedPropertiesFileEntry = loadTypedPropertiesGetTestEntry(true);
		Properties propertiesToEdit = typedPropertiesFileEntry.getValue();
		propertiesToEdit.put(STRING_KEY, "edited_string_value");
		try (FileOutputStream fos = new FileOutputStream(typedPropertiesFileEntry.getKey())) {
			propertiesToEdit.store(fos, null);
		}
		Thread.sleep(PropertiesInjector.getPollInterval() + 500);
		checkProperties(typedPropertiesFileEntry);
	}

	@Test
	public void injectString() {
		String propertyValue = loadTypedPropertiesGetTestValue(STRING_KEY, false);
		assertTrue(propertyValue.equals(annotatedString));
	}

	@Test
	public void injectInteger() {
		Integer propertyValue = new Integer(loadTypedPropertiesGetTestValue(INTEGER_KEY, false));
		assertTrue(propertyValue.equals(annotatedInteger));
	}

	@Test
	public void injectLong() {
		Long propertyValue = new Long(loadTypedPropertiesGetTestValue(LONG_KEY, false));
		assertTrue(propertyValue.equals(annotatedLong));
	}

	@Test
	public void injectBigDecimal() {
		BigDecimal propertyValue = new BigDecimal(loadTypedPropertiesGetTestValue(BIGDECIMAL_KEY, false));
		assertTrue(propertyValue.compareTo(annotatedBigDecimal) == 0);
	}

	@Test
	public void injectDouble() {
		Double propertyValue = new Double(loadTypedPropertiesGetTestValue(DOUBLE_KEY, false));
		assertTrue(propertyValue.equals(annotatedDouble));
	}

	@Test
	public void injectDate() {
		try {
			Date propertyValue = new SimpleDateFormat().parse(loadTypedPropertiesGetTestValue(DATE_KEY, false));
			assertTrue(propertyValue.equals(annotatedDate));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private String loadTypedPropertiesGetTestValue(String propertyKey, boolean hotReload) {
		Entry<String, Properties> typedPropertiesFileEntry = loadTypedPropertiesGetTestEntry(hotReload);
		return typedPropertiesFileEntry.getValue().getProperty(propertyKey);
	}

	private Entry<String, Properties> loadTypedPropertiesGetTestEntry(boolean hotReload) {
		Entry<String, Properties> typedPropertiesFileEntry = null;
		for (Entry<String, Properties> propertiesFileEntry : PROPERTIES_FILES_MAP.entrySet()) {
			if (propertiesFileEntry.getKey().contains(TYPED_PROPERTIES_LABEL)) {
				typedPropertiesFileEntry = propertiesFileEntry;
				break;
			}
		}
		PropertiesInjector.loadProperties(hotReload, typedPropertiesFileEntry.getKey());
		return typedPropertiesFileEntry;
	}

	private void checkProperties(Entry<String, Properties> propertyFileEntry) {
		Properties inMemoryProperties = propertyFileEntry.getValue();
		for (Object key : inMemoryProperties.keySet()) {
			assertTrue(inMemoryProperties.get(key).equals(PropertiesInjector.getProperty((String) key)));
		}
	}

}
