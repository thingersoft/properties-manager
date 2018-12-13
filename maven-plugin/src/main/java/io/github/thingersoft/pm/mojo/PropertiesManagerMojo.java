package io.github.thingersoft.pm.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;

import io.github.thingersoft.pm.mojo.jtwig.JoinAndWrapJtwigFunction;
import io.github.thingersoft.pm.mojo.jtwig.ToUncapitalizedCamelCaseJtwigFunction;

@Mojo(name = PropertiesManagerMojo.GOAL)
public class PropertiesManagerMojo extends AbstractMojo {

	public static final String GOAL = "map";
	public static final String GENERATED_CLASS_NAME = "ApplicationProperties";

	@Parameter
	private List<File> propertiesLocations;
	@Parameter
	private List<String> propertiesLocationsVariables;
	@Parameter(defaultValue = "")
	private String datePattern;
	@Parameter(defaultValue = "")
	private String locale;
	@Parameter(defaultValue = "true")
	private boolean hotReload;
	@Parameter(defaultValue = "")
	private String obfuscatedPropertyPattern;
	@Parameter(defaultValue = "")
	private String obfuscatedPropertyPlaceholder;

	@Parameter(required = true)
	private List<File> templateFiles;
	@Parameter(required = true)
	private String basePackage;
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/properties-manager")
	private File generatedSourcesDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		generateSources(templateFiles, generatedSourcesDirectory, basePackage, datePattern, locale, obfuscatedPropertyPattern, obfuscatedPropertyPlaceholder,
				hotReload, propertiesLocations, propertiesLocationsVariables);
	}

	public void generateSources(List<File> templateFiles, File generatedSourcesDirectory, String basePackage, String datePattern, String locale,
			String obfuscatedPropertyPattern, String obfuscatedPropertyPlaceholder, boolean hotReload, List<File> propertiesLocations,
			List<String> propertiesLocationsVariables) {

		// read properties files and merge them into a single map
		Properties templateProperties = new Properties();
		for (File templateFile : templateFiles) {
			Properties templateFileProperties = new Properties();
			try (FileInputStream fis = new FileInputStream(templateFile)) {
				templateFileProperties.load(fis);
				templateProperties.putAll(templateFileProperties);
			} catch (IOException e) {
				throw new RuntimeException("Can't read properties template file", e);
			}
		}

		List<String> propertiesLocationsStrings = new ArrayList<>();
		for (File propertiesLocation : propertiesLocations) {
			propertiesLocationsStrings.add(propertiesLocation.toString().replace("\\", "/"));
		}

		// generate source file
		final EnvironmentConfiguration jTwigEnv = EnvironmentConfigurationBuilder.configuration().functions().add(new ToUncapitalizedCamelCaseJtwigFunction())
				.add(new JoinAndWrapJtwigFunction()).and().build();
		JtwigTemplate template = JtwigTemplate.classpathTemplate("/ApplicationProperties.twig", jTwigEnv);
		JtwigModel model = JtwigModel.newModel().with("basePackage", basePackage).with("datePattern", datePattern).with("locale", locale)
				.with("obfuscatedPropertyPattern", obfuscatedPropertyPattern).with("obfuscatedPropertyPlaceholder", obfuscatedPropertyPlaceholder)
				.with("hotReload", hotReload).with("properties", templateProperties.keySet()).with("propertiesLocations", propertiesLocationsStrings)
				.with("propertiesLocationsVariables", propertiesLocationsVariables);
		try {
			Path outputDirectoryPath = Files.createDirectories(Paths.get(generatedSourcesDirectory.getAbsolutePath(), basePackage.replaceAll("\\.", "/")));
			Path outputFilePath = outputDirectoryPath.resolve(GENERATED_CLASS_NAME + ".java");
			try (OutputStream os = Files.newOutputStream(outputFilePath)) {
				template.render(model, os);
			}
		} catch (IOException e) {
			throw new RuntimeException("Can't write to output folder", e);
		}
	}

}
