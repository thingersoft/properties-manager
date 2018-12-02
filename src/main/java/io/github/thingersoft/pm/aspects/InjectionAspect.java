package io.github.thingersoft.pm.aspects;

import java.lang.reflect.Field;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import io.github.thingersoft.pm.PropertiesInjector;
import io.github.thingersoft.pm.SupportedTypes;
import io.github.thingersoft.pm.annotations.Property;

@Aspect
public class InjectionAspect {

	@Before("get(@io.github.thingersoft.pm.annotations.Property * *)")
	public void beforePropertyFieldAccess(JoinPoint joinPoint)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = joinPoint.getSignature().getDeclaringType().getDeclaredField(joinPoint.getSignature().getName());

		SupportedTypes supportedType = SupportedTypes.getSupportedType(field.getType());
		if (supportedType == null) {
			throw new RuntimeException("Unsupported field type: " + field.getType());
		}

		String propertyKey = field.getAnnotation(Property.class).value();
		Object propertyValue = null;

		switch (supportedType) {
		case BIGDECIMAL:
			propertyValue = PropertiesInjector.getBigDecimal(propertyKey);
			break;
		case DATE:
			propertyValue = PropertiesInjector.getDate(propertyKey);
			break;
		case DOUBLE:
			propertyValue = PropertiesInjector.getDouble(propertyKey);
			break;
		case FLOAT:
			propertyValue = PropertiesInjector.getFloat(propertyKey);
			break;
		case INTEGER:
			propertyValue = PropertiesInjector.getInteger(propertyKey);
			break;
		case LONG:
			propertyValue = PropertiesInjector.getLong(propertyKey);
			break;
		case STRING:
			propertyValue = PropertiesInjector.getProperty(propertyKey);
			break;
		}

		field.setAccessible(true);
		field.set(joinPoint.getTarget(), propertyValue);
	}

}
