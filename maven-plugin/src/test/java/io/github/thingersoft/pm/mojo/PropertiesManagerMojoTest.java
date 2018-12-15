package io.github.thingersoft.pm.mojo;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.Test;

import io.github.thingersoft.pm.api.PropertiesStore;
import io.github.thingersoft.pm.api.data.PropertiesStoreOptions;
import io.github.thingersoft.pm.api.data.SupportedTypes;

public class PropertiesManagerMojoTest {

	private static final String TEST_PACKAGE = "test";

	private static final String DATE_PATTERN = "dd/MM/yyyy";
	private static final String DATE_KEY = "pm-test.date_key_1";
	private static final String DATE_FIELD_NAME = "customDateField";

	@Test
	public void generateSourcesTest() throws Exception {
		PropertiesManagerMojo mojo = new PropertiesManagerMojo();

		File testPropertiesFile = new File(ClassLoader.getSystemResource("test.properties").getFile());
		Properties testProperties = new Properties();
		try (FileInputStream fis = new FileInputStream(testPropertiesFile)) {
			testProperties.load(fis);
		}
		Path tempDir = Files.createDirectories(FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir") + "/properties_manager_plugin"));

		PropertiesStoreOptions options = new PropertiesStoreOptions();
		options.setDatePattern(DATE_PATTERN);
		List<FieldMapping> fieldMappings = new ArrayList<>();
		FieldMapping customDateFieldMapping = new FieldMapping();
		customDateFieldMapping.setPropertyKey(DATE_KEY);
		customDateFieldMapping.setFieldName(DATE_FIELD_NAME);
		customDateFieldMapping.setFieldtype(SupportedTypes.DATE);
		fieldMappings.add(customDateFieldMapping);
		mojo.generateSources(Arrays.asList(testPropertiesFile, testPropertiesFile), new File(tempDir.toString()), TEST_PACKAGE, options,
				Arrays.asList(testPropertiesFile.toString(), testPropertiesFile.toString()), fieldMappings);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		String generatedSourceBaseFilePath = tempDir.resolve(TEST_PACKAGE + "/" + PropertiesManagerMojo.GENERATED_CLASS_NAME).toString();
		assertTrue(compiler.run(null, null, null, generatedSourceBaseFilePath + ".java") == 0);

		try (URLClassLoader classLoader = new URLClassLoader(new URL[] { tempDir.toUri().toURL() })) {
			Class<?> generatedClass = classLoader.loadClass(TEST_PACKAGE + "." + PropertiesManagerMojo.GENERATED_CLASS_NAME);
			PropertiesStore.initByAnnotatedClass(generatedClass);

			for (Entry<Object, Object> testProperty : testProperties.entrySet()) {
				String key = (String) testProperty.getKey();
				if (key.equals(DATE_KEY)) {
					assertTrue(generatedClass.getDeclaredField(DATE_FIELD_NAME).get(null).equals(PropertiesStore.getDate(DATE_KEY)));
				} else {
					assertTrue(testProperty.getValue().equals(PropertiesStore.getProperty(key)));

				}
			}
		}

	}

}
