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
	 * @see PropertiesStore#loadProperties
	 */
	String[] propertiesLocations() default {};

	/**
	 * @see PropertiesStoreOptions#setHotReload(boolean)
	 */
	boolean hotReload() default true;

	/**
	 * @see PropertiesStoreOptions#setDatePattern(String)
	 */
	String datePattern() default "";

	/**
	 * @see PropertiesStoreOptions#setLocale(String)
	 */
	String locale() default "";

	/**
	 * @see PropertiesStoreOptions#setObfuscatedPropertyPattern(String)
	 */
	String obfuscatedPropertyPattern() default "";

	/**
	 * @see PropertiesStoreOptions#setObfuscatedPropertyPlaceholder(String)
	 */
	String obfuscatedPropertyPlaceholder() default "";

}
