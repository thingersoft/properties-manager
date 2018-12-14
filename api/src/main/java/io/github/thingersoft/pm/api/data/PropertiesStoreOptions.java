package io.github.thingersoft.pm.api.data;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import io.github.thingersoft.pm.api.PropertiesStore;

/**
 * Encapsulates {@link PropertiesStore} options 
 */
public class PropertiesStoreOptions {

	private String datePattern = new SimpleDateFormat().toPattern();
	private Locale locale = Locale.getDefault();
	private boolean hotReload = true;
	private String obfuscatedPropertyPattern;
	private String obfuscatedPropertyPlaceholder = "******";

	/**
	 * Sets the key pattern of sensitive properties to be obfuscated by the {@link PropertiesStore#toText()} method.
	 * 
	 * @param obfuscatedPropertyPattern
	 * regular expression of sensitive properties keys
	 */
	public void setObfuscatedPropertyPattern(String obfuscatedPropertyPattern) {
		if (StringUtils.isNotBlank(obfuscatedPropertyPattern)) {
			this.obfuscatedPropertyPattern = obfuscatedPropertyPattern;
		}
	}

	/**
	 * Sets the placeholder to be used for sensitive properties values by the {@link PropertiesStore#toText()} method.
	 * 
	 * @param obfuscatedPropertyPlaceholder
	 * placeholder {@code String} for sensitive properties values
	 */
	public void setObfuscatedPropertyPlaceholder(String obfuscatedPropertyPlaceholder) {
		if (StringUtils.isNotBlank(obfuscatedPropertyPlaceholder)) {
			this.obfuscatedPropertyPlaceholder = obfuscatedPropertyPlaceholder;
		}
	}

	/**
	 * Sets the pattern to be used for dates parsing
	 * 
	 * @param datePattern
	 */
	public void setDatePattern(String datePattern) {
		if (StringUtils.isNotBlank(datePattern)) {
			this.datePattern = datePattern;
		}
	}

	/**
	 * Sets the locale to be used for dates parsing
	 * 
	 * @param locale
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @param language
	 * 
	 * @see #setLocale(Locale)
	 */
	public void setLocale(String language) {
		if (StringUtils.isNotBlank(language)) {
			this.locale = new Locale(language);
		}
	}

	/**
	 * Enables properties files live monitoring
	 * 
	 * @param hotReload
	 */
	public void setHotReload(boolean hotReload) {
		this.hotReload = hotReload;
	}

	public String getObfuscatedPropertyPlaceholder() {
		return obfuscatedPropertyPlaceholder;
	}

	public String getObfuscatedPropertyPattern() {
		return obfuscatedPropertyPattern;
	}

	public String getDatePattern() {
		return datePattern;
	}

	public Locale getLocale() {
		return locale;
	}

	public boolean isHotReload() {
		return hotReload;
	}

}
