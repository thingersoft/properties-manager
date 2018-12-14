package io.github.thingersoft.pm.mojo;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.Test;

import io.github.thingersoft.pm.api.PropertiesStore;
import io.github.thingersoft.pm.api.data.PropertiesStoreOptions;

public class PropertiesManagerMojoTest {

	private static final String TEST_PACKAGE = "test";

	@Test
	public void generateSourcesTest() throws Exception {
		PropertiesManagerMojo mojo = new PropertiesManagerMojo();

		File testPropertiesFile = new File(ClassLoader.getSystemResource("test.properties").getFile());
		Properties testProperties = new Properties();
		try (FileInputStream fis = new FileInputStream(testPropertiesFile)) {
			testProperties.load(fis);
		}
		Path tempDir = Files.createDirectories(FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir") + "/properties_manager_plugin"));

		mojo.generateSources(Arrays.asList(testPropertiesFile, testPropertiesFile), new File(tempDir.toString()), TEST_PACKAGE, new PropertiesStoreOptions(),
				Arrays.asList(testPropertiesFile, testPropertiesFile), null);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		String generatedSourceBaseFilePath = tempDir.resolve(TEST_PACKAGE + "/" + PropertiesManagerMojo.GENERATED_CLASS_NAME).toString();
		assertTrue(compiler.run(null, null, null, generatedSourceBaseFilePath + ".java") == 0);

		try (URLClassLoader classLoader = new URLClassLoader(new URL[] { tempDir.toUri().toURL() })) {
			Class<?> generatedClass = classLoader.loadClass(TEST_PACKAGE + "." + PropertiesManagerMojo.GENERATED_CLASS_NAME);
			PropertiesStore.initByAnnotatedClass(generatedClass);

			for (Entry<Object, Object> testProperty : testProperties.entrySet()) {
				assertTrue(testProperty.getValue().equals(PropertiesStore.getProperty((String) testProperty.getKey())));
			}
		}

	}

}
