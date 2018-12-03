package io.github.thingersoft.pm.aspects;

import java.lang.reflect.Field;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import io.github.thingersoft.pm.PropertiesStore;
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
			propertyValue = PropertiesStore.getBigDecimal(propertyKey);
			break;
		case DATE:
			propertyValue = PropertiesStore.getDate(propertyKey);
			break;
		case DOUBLE:
			propertyValue = PropertiesStore.getDouble(propertyKey);
			break;
		case FLOAT:
			propertyValue = PropertiesStore.getFloat(propertyKey);
			break;
		case INTEGER:
			propertyValue = PropertiesStore.getInteger(propertyKey);
			break;
		case LONG:
			propertyValue = PropertiesStore.getLong(propertyKey);
			break;
		case STRING:
			propertyValue = PropertiesStore.getProperty(propertyKey);
			break;
		}

		field.setAccessible(true);
		field.set(joinPoint.getTarget(), propertyValue);
	}

}
