/**
 * 
 */
package io.github.thingersoft.pm.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.thingersoft.pm.api.PropertiesStore;

/**
 * Annotation to be used on types for declarative style properties configuration and injection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Properties {

	/**
	 * Array of properties file system locations
	 */
	String[] propertiesLocations() default {};

	/**
	 * Array of system or environment variables containing properties file system locations
	 */
	String[] propertiesLocationsVariables() default {};

	/**
	 * @see PropertiesStore#setHotReload(boolean)
	 */
	boolean hotReload() default true;

	/**
	 * @see PropertiesStore#setDatePattern(String)
	 */
	String datePattern() default "";

	/**
	 * @see PropertiesStore#setLocale(String)
	 */
	String locale() default "";

	/**
	 * @see PropertiesStore#setObfuscatedPropertyPattern(String)
	 */
	String obfuscatedPropertyPattern() default "";

	/**
	 * @see PropertiesStore#setObfuscatedPropertyPlaceholder(String)
	 */
	String obfuscatedPropertyPlaceholder() default "";

}
