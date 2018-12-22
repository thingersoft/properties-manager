/**
 * 
 */
package io.github.thingersoft.pm.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.thingersoft.pm.api.PropertiesStore;
import io.github.thingersoft.pm.api.data.PropertiesStoreOptions;

/**
 * Annotation to be used on types for declarative style properties configuration and injection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Properties {

	/**
	 * Array of properties file system locations.<br>
	 * May contain system and/or environment variables enclosed by curly braces.
	 * 
	 * @return properties locations
	 * 
	 * @see PropertiesStore#loadProperties
	 */
	String[] propertiesLocations() default {};

	/**
	 * @return hot reload flag
	 * 
	 * @see PropertiesStoreOptions#setHotReload(boolean)
	 * 
	 */
	boolean hotReload() default true;

	/**
	 * @return dates pattern
	 * 
	 * @see PropertiesStoreOptions#setDatePattern(String)
	 */
	String datePattern() default "";

	/**
	 * @return language code
	 * 
	 * @see PropertiesStoreOptions#setLocale(String)
	 */
	String locale() default "";

	/**
	 * @return obfuscated keys regex
	 * 
	 * @see PropertiesStoreOptions#setObfuscatedPropertyPattern(String)
	 */
	String obfuscatedPropertyPattern() default "";

	/**
	 * @return obfuscated keys placeholder
	 * 
	 * @see PropertiesStoreOptions#setObfuscatedPropertyPlaceholder(String)
	 */
	String obfuscatedPropertyPlaceholder() default "";

}
