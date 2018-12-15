package io.github.thingersoft.pm.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public final class PropertiesTestUtility {

	private PropertiesTestUtility() {

	}

	private static final String STRING_PROPERTIES_LABEL = "string_properties_file";
	public static final String TYPED_PROPERTIES_LABEL = "typed_properties_file";
	public static final String DATE_KEY = "date_key";
	public static final String DOUBLE_KEY = "double_key";
	public static final String FLOAT_KEY = "float_key";
	public static final String BIGDECIMAL_KEY = "bigdecimal_key";
	public static final String LONG_KEY = "long_key";
	public static final String INTEGER_KEY = "integer_key";
	public static final String STRING_KEY = "string_key";
	public static final Map<String, Properties> PROPERTIES_FILES_MAP = new LinkedHashMap<>();

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

	public static Entry<String, Properties> getTypedPropertiesTestEntry() {
		Entry<String, Properties> typedPropertiesFileEntry = null;
		for (Entry<String, Properties> propertiesFileEntry : PROPERTIES_FILES_MAP.entrySet()) {
			if (propertiesFileEntry.getKey().contains(TYPED_PROPERTIES_LABEL)) {
				typedPropertiesFileEntry = propertiesFileEntry;
				break;
			}
		}
		return typedPropertiesFileEntry;
	}

}