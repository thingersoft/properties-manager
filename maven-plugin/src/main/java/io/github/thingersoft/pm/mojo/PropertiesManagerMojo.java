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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;

import io.github.thingersoft.pm.api.data.PropertiesStoreOptions;
import io.github.thingersoft.pm.api.data.SupportedTypes;
import io.github.thingersoft.pm.mojo.jtwig.JoinAndWrapJtwigFunction;

@Mojo(name = PropertiesManagerMojo.GOAL)
public class PropertiesManagerMojo extends AbstractMojo {

	public static final String GOAL = "map";
	public static final String GENERATED_CLASS_NAME = "ApplicationProperties";

	@Parameter
	private List<File> propertiesLocations;
	@Parameter
	private List<String> propertiesLocationsVariables;
	@Parameter
	private PropertiesStoreOptions options;
	@Parameter(required = true)
	private List<File> templateFiles;
	@Parameter(required = true)
	private String basePackage;
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/properties-manager")
	private File generatedSourcesDirectory;

	@Parameter
	private List<FieldMapping> fieldMappings;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		generateSources(templateFiles, generatedSourcesDirectory, basePackage, options, propertiesLocations, propertiesLocationsVariables, fieldMappings);
	}

	public void generateSources(List<File> templateFiles, File generatedSourcesDirectory, String basePackage, PropertiesStoreOptions options,
			List<File> propertiesLocations, List<String> propertiesLocationsVariables, List<FieldMapping> fieldMappings) {

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

		// convert backslashes style paths to forward slashes
		List<String> propertiesLocationsStrings = new ArrayList<>();
		for (File propertiesLocation : propertiesLocations) {
			propertiesLocationsStrings.add(propertiesLocation.toString().replace("\\", "/"));
		}

		// merge custom field mappings (if provided) with defaults
		List<FieldMapping> computedFieldMappings = new ArrayList<>();
		if (fieldMappings != null) {
			computedFieldMappings.addAll(fieldMappings);
		}
		for (Object key : templateProperties.keySet()) {
			String stringKey = (String) key;
			FieldMapping fieldMapping = new FieldMapping();
			fieldMapping.setPropertyKey(stringKey);
			fieldMapping.setFieldName(toUncapitalizedCamelCase(stringKey));
			fieldMapping.setFieldtype(SupportedTypes.STRING);
			if (!computedFieldMappings.contains(fieldMapping)) {
				computedFieldMappings.add(fieldMapping);
			}
		}

		// generate source file
		final EnvironmentConfiguration jTwigEnv = EnvironmentConfigurationBuilder.configuration().functions().add(new JoinAndWrapJtwigFunction()).and().build();
		JtwigTemplate template = JtwigTemplate.classpathTemplate("/ApplicationProperties.twig", jTwigEnv);
		JtwigModel model = JtwigModel.newModel().with("basePackage", basePackage).with("fieldMappings", computedFieldMappings)
				.with("propertiesLocations", propertiesLocationsStrings).with("propertiesLocationsVariables", propertiesLocationsVariables)
				.with("options", options);
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

	private String toUncapitalizedCamelCase(String inputString) {
		String[] splittedInputString = inputString.split("[_\\-\\.]");
		String outputString = StringUtils.uncapitalize(splittedInputString[0]);
		for (int i = 1; i < splittedInputString.length; i++) {
			outputString += StringUtils.capitalize(splittedInputString[i]);
		}
		return outputString;
	}

}
