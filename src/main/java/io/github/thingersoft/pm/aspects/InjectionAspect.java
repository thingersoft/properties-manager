package io.github.thingersoft.pm.aspects;

import java.lang.reflect.Field;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import io.github.thingersoft.pm.PropertiesInjector;
import io.github.thingersoft.pm.annotations.Property;

@Aspect
public class InjectionAspect {

	@Before("get(@io.github.thingersoft.pm.annotations.Property String *)")
	public void beforePropertyFieldAccess(JoinPoint joinPoint)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = joinPoint.getSignature().getDeclaringType().getDeclaredField(joinPoint.getSignature().getName());
		field.setAccessible(true);
		field.set(joinPoint.getTarget(), PropertiesInjector.getProperty(field.getAnnotation(Property.class).value()));
	}

}
