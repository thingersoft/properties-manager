package io.github.thingersoft.pm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * Properties store facility.
 */
public class ApplicationProperties extends Properties {
	private static final long serialVersionUID = 1L;

	private String obfuscatedPropertyPattern;
	private String obfuscatedPropertyPlaceholder = "******";

	/**
	 * Returns a string representation of this {@code ApplicationProperties} object
	 * in the form of a set of entries, enclosed in braces and separated
	 * by the ASCII characters "{@code ,} " (comma and space).<br>
	 * Each entry is rendered as the key, an equals sign {@code =}, and the
	 * associated string value.<br>
	 * If the key matches the {@code obfuscatedPropertyPattern} the value will be replaced by the {@code obfuscatedPropertyPlaceholder}.<br>
	 * 
	 * @see 
	 * ApplicationProperties#setObfuscatedPropertyPattern(String)
	 * @see 
	 * ApplicationProperties#setObfuscatedPropertyPlaceholder(String)
	 *
	 * @return 
	 * a string representation of this {@code ApplicationProperties}
	 */
	@Override
	public synchronized String toString() {
		List<String> properties = new ArrayList<>();
		for (Entry<Object, Object> property : entrySet()) {
			String key = property.getKey().toString();
			String value = obfuscatedPropertyPattern != null && key.matches(obfuscatedPropertyPattern) ? obfuscatedPropertyPlaceholder
					: "" + property.getValue();
			properties.add(key + "=" + value);
		}
		return "{" + StringUtils.join(properties, ", ") + "}";
	}

	/**
	 * Sets the key pattern of sensitive properties to be obfuscated by the {@link ApplicationProperties#toString()} method.
	 * 
	 * @param obfuscatedPropertyPattern
	 * regular expression of sensitive properties keys
	 */
	public void setObfuscatedPropertyPattern(String obfuscatedPropertyPattern) {
		this.obfuscatedPropertyPattern = obfuscatedPropertyPattern;
	}

	/**
	 * Sets the placeholder to be used for sensitive properties values by the {@link ApplicationProperties#toString()} method.
	 * 
	 * @param obfuscatedPropertyPlaceholder
	 * placeholder {@code String} for sensitive properties values
	 */
	public void setObfuscatedPropertyPlaceholder(String obfuscatedPropertyPlaceholder) {
		this.obfuscatedPropertyPlaceholder = obfuscatedPropertyPlaceholder;
	}

	public String getObfuscatedPropertyPlaceholder() {
		return obfuscatedPropertyPlaceholder;
	}

	public String getObfuscatedPropertyPattern() {
		return obfuscatedPropertyPattern;
	}

}