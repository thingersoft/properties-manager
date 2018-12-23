# Properties Manager

[![Maven Central](https://img.shields.io/maven-central/v/io.github.thingersoft/properties-manager-api.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.thingersoft%22%20AND%20a:%22properties-manager-api%22)
[![Build Status](https://travis-ci.com/thingersoft/properties-manager.svg?branch=master)](https://travis-ci.com/thingersoft/properties-manager)

A container agnostic tool for application wide configuration through properties files.


### Features
  - Hot reloading
  - Multiple source files aggregation
  - Automatic property type conversion
  - Declarative + programmatic API
  - Properties mapping generator  
  
  
### Dependency

```xml
<dependency>
    <groupId>io.github.thingersoft</groupId>
    <artifactId>properties-manager-api</artifactId>
    <version>LATEST</version>
</dependency>
```
  
    
### Usage

Properties manager offers both declarative and programmatic APIs that can also be mixed together.  
For the following examples we'll suppose to deal with this properties file:

```properties
sample.string = xxx
sample.integer = 2
sample.date = 01/01/1970
```

<br/>

##### Declarative API

Configure the Properties Manager maven plugin:

```xml
<plugin>
    <groupId>io.github.thingersoft</groupId>
    <artifactId>properties-manager-maven-plugin</artifactId>
    <version>LATEST</version>
    <executions>
        <execution>
            <goals>
                <goal>enhance</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Create a mapping class:

```java
@Properties(propertiesLocations = { "{sample.config.dir}/sample.properties" }, datePattern = "dd/MM/yyyy")
public class SampleProperties {

    @Property("sample.string")
    public static String sampleString;
    @Property("sample.integer")
    public static Integer sampleInteger;
    @Property("sample.date")
    public static Date sampleDate;

}
```

And that's it, the @Property annotated static fields will get injected with up to date properties values.  
The @Properties type level annotation attributes can be used for configuration.  

<br/>

##### Automatic mapping class generation

The Properties Manager maven plugin also features automatic mapping through the "generate" goal:

```xml
<plugin>
    <groupId>io.github.thingersoft</groupId>
    <artifactId>properties-manager-maven-plugin</artifactId>
    <version>LATEST</version>
    <executions>
        <execution>
            <id>enhance</id>
            <goals>
                <goal>enhance</goal>
            </goals>
        </execution>
        <execution>
            <id>generate</id>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <basePackage>com.sample</basePackage>
                <propertiesLocations>
                    <propertiesLocation>{sample.config.dir}/sample.properties</propertiesLocation>
                </propertiesLocations>
                <templateFiles>
                    <templateFile>${project.parent.basedir}/config/sample.properties</templateFile>
                </templateFiles>
            </configuration>
        </execution>    
    </executions>
</plugin>
```


The above configuration will generate the following class and add it to your sources:

```java
@Properties(
    propertiesLocations = { "{sample.config.dir}/sample.properties" },
    hotReload = true, 
    datePattern = "dd/MM/yy H.mm", 
    locale = "en_US", 
    obfuscatedPropertyPattern = "", 
    obfuscatedPropertyPlaceholder = "******"
)
public class SampleProperties {

    @Property("sample.string")
    public static String sampleString;
    @Property("sample.integer")
    public static String sampleInteger;
    @Property("sample.date")
    public static String sampleDate;

}
```

<br/>
By default the generator will map properties to String fields, whose name will be inferred by converting property keys into camel case.
<br/>
You can customize mapping behaviour and overall options through plugin configuration:

```xml
<plugin>
    <groupId>io.github.thingersoft</groupId>
    <artifactId>properties-manager-maven-plugin</artifactId>
    <version>LATEST</version>
    <executions>
        <execution>
            <id>generate</id>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <basePackage>com.sample</basePackage>
                <propertiesLocations>
                    <propertiesLocation>{sample.config.dir}/sample.properties</propertiesLocation>
                </propertiesLocations>
                <templateFiles>
                    <templateFile>${project.parent.basedir}/config/sample.properties</templateFile>
                </templateFiles>
                <options>
                    <datePattern>dd/MM/yyyy</datePattern>
                </options>
                <fieldMappings>
                    <fieldMapping>
                        <fieldName>customDateField</fieldName>
                        <fieldtype>DATE</fieldtype>
                        <propertyKey>sample.date</propertyKey>
                    </fieldMapping>
                </fieldMappings>
            </configuration>
        </execution>
        <execution>
            <id>enhance</id>
            <goals>
                <goal>enhance</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
The above configuration will generate:

```java
@Properties(
    propertiesLocations = { "{sample.config.dir}/sample.properties" },
    hotReload = true, 
    datePattern = "dd/MM/yyyy", 
    locale = "en_US", 
    obfuscatedPropertyPattern = "", 
    obfuscatedPropertyPlaceholder = "******"
)
public class SampleProperties {

    @Property("sample.string")
    public static String sampleString;
    @Property("sample.integer")
    public static String sampleInteger;
    @Property("sample.date")
    public static Date customDateField;

}
```

<br/>

##### Programmatic API

```java
PropertiesStore.getOptions().setDatePattern("dd/MM/yyyy");
PropertiesStore.loadProperties("etc/sample.properties");

String stringProperty = PropertiesStore.getProperty("sample.string");
Date dateProperty = PropertiesStore.getDate("sample.date");		
```

<br/>

See javadocs for more details and available options.
