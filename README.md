# Properties Manager

[![Build Status](https://travis-ci.com/thingersoft/properties-manager.svg?branch=master)](https://travis-ci.com/thingersoft/properties-manager)

A container agnostic tool for application wide configuration through properties files.

### Features
  - Hot reloading
  - Multiple source files aggregation
  - Declarative + programmatic properties injection
  - Automatic type conversion

### Usage

```java
@Properties(propertiesLocationsVariables = { "sample_system_property" })
public class SampleProperties {

	@Property("sample.integer")
	public static Integer annotatedInteger;
	@Property("sample.date")
	public static Date annotatedDate;

}
```

sample.properties:

```properties
sample.integer = 2
sample.date = 01/01/1970
```

