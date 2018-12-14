package io.github.thingersoft.pm.mojo;

import io.github.thingersoft.pm.api.data.SupportedTypes;

public class FieldMapping {

	private String propertyKey;
	private String fieldName;
	private SupportedTypes fieldtype;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyKey == null) ? 0 : propertyKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldMapping other = (FieldMapping) obj;
		if (propertyKey == null) {
			if (other.propertyKey != null)
				return false;
		} else if (!propertyKey.equals(other.propertyKey))
			return false;
		return true;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public SupportedTypes getFieldtype() {
		return fieldtype;
	}

	public void setFieldtype(SupportedTypes fieldtype) {
		this.fieldtype = fieldtype;
	}

	public String getPropertyKey() {
		return propertyKey;
	}

	public void setPropertyKey(String propertyKey) {
		this.propertyKey = propertyKey;
	}

}
