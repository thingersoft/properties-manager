/**
 * 
 */
package io.github.thingersoft.pm.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used on types for declarative style properties configuration and injection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Properties {

	String[] propertiesLocations() default {};

	String[] propertiesLocationsVariables() default {};

	boolean hotReload() default true;

	String datePattern() default "";

	String locale() default "";

	String obfuscatedPropertyPattern() default "";

	String obfuscatedPropertyPlaceholder() default "";

}
