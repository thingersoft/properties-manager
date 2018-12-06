package io.github.thingersoft.pm.api;

import java.math.BigDecimal;
import java.util.Date;

public enum SupportedTypes {

	STRING(String.class), INTEGER(Integer.class), LONG(Long.class), BIGDECIMAL(BigDecimal.class), FLOAT(Float.class), DOUBLE(Double.class), DATE(Date.class);

	public static SupportedTypes getSupportedType(Class<?> clazz) {
		for (SupportedTypes supportedType : SupportedTypes.values()) {
			if (supportedType.getClazz().equals(clazz)) {
				return supportedType;
			}
		}
		return null;
	}

	private Class<?> clazz;

	private SupportedTypes(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Class<?> getClazz() {
		return clazz;
	}

}
