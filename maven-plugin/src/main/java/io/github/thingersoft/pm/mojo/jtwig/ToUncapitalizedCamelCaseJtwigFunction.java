package io.github.thingersoft.pm.mojo.jtwig;

import org.apache.commons.lang3.StringUtils;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

public class ToUncapitalizedCamelCaseJtwigFunction extends SimpleJtwigFunction {

	@Override
	public String name() {
		return "toUncapitalizedCamelCase";
	}

	@Override
	public Object execute(FunctionRequest request) {
		String inputString = (String) request.get(0);
		String[] splittedInputString = inputString.split("[_\\-\\.]");
		String outputString = StringUtils.uncapitalize(splittedInputString[0]);
		for (int i = 1; i < splittedInputString.length; i++) {
			outputString += StringUtils.capitalize(splittedInputString[i]);
		}
		return outputString;
	}

}