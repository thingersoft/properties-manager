package io.github.thingersoft.pm.aspectj;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.thingersoft.pm.api.PropertiesStore;
import io.github.thingersoft.pm.api.annotations.Property;
import io.github.thingersoft.pm.test.PropertiesTest;

public class InjectionAspectTest {

	@Property(PropertiesTest.STRING_KEY)
	private String annotatedString;
	@Property(PropertiesTest.INTEGER_KEY)
	private Integer annotatedInteger;
	@Property(PropertiesTest.LONG_KEY)
	private Long annotatedLong;
	@Property(PropertiesTest.BIGDECIMAL_KEY)
	private BigDecimal annotatedBigDecimal;
	@Property(PropertiesTest.FLOAT_KEY)
	private Float annotatedFloat;
	@Property(PropertiesTest.DOUBLE_KEY)
	private Double annotatedDouble;
	@Property(PropertiesTest.DATE_KEY)
	private Date annotatedDate;

	@BeforeClass
	public static void init() throws IOException {
		PropertiesTest.initProperties();
	}

	@After
	public void reset() {
		PropertiesStore.reset();
	}

	@Test
	public void injectString() {
		String propertyValue = loadTypedPropertiesGetTestValue(PropertiesTest.STRING_KEY, false);
		assertTrue(propertyValue.equals(annotatedString));
	}

	@Test
	public void injectInteger() {
		Integer propertyValue = new Integer(loadTypedPropertiesGetTestValue(PropertiesTest.INTEGER_KEY, false));
		assertTrue(propertyValue.equals(annotatedInteger));
	}

	@Test
	public void injectLong() {
		Long propertyValue = new Long(loadTypedPropertiesGetTestValue(PropertiesTest.LONG_KEY, false));
		assertTrue(propertyValue.equals(annotatedLong));
	}

	@Test
	public void injectBigDecimal() {
		BigDecimal propertyValue = new BigDecimal(loadTypedPropertiesGetTestValue(PropertiesTest.BIGDECIMAL_KEY, false));
		assertTrue(propertyValue.compareTo(annotatedBigDecimal) == 0);
	}

	@Test
	public void injectDouble() {
		Double propertyValue = new Double(loadTypedPropertiesGetTestValue(PropertiesTest.DOUBLE_KEY, false));
		assertTrue(propertyValue.equals(annotatedDouble));
	}

	@Test
	public void injectDate() {
		try {
			Date propertyValue = new SimpleDateFormat().parse(loadTypedPropertiesGetTestValue(PropertiesTest.DATE_KEY, false));
			assertTrue(propertyValue.equals(annotatedDate));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private String loadTypedPropertiesGetTestValue(String propertyKey, boolean hotReload) {
		Entry<String, Properties> typedPropertiesFileEntry = PropertiesTest.getTypedPropertiesTestEntry();
		PropertiesStore.loadProperties(hotReload, typedPropertiesFileEntry.getKey());
		return typedPropertiesFileEntry.getValue().getProperty(propertyKey);
	}

}
