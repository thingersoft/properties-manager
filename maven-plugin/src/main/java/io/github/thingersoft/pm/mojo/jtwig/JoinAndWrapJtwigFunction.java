package io.github.thingersoft.pm.mojo.jtwig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;
import org.jtwig.util.FunctionValueUtils;

public class JoinAndWrapJtwigFunction extends SimpleJtwigFunction {

	@Override
	public String name() {
		return "joinAndWrap";
	}

	@Override
	public Object execute(FunctionRequest request) {
		if (request.get(0) != null) {
			Collection<Object> inputStrings = FunctionValueUtils.getCollection(request, 0);
			String separator = (String) request.get(1);
			String wrapper = (String) request.get(2);

			List<String> wrappedStrings = new ArrayList<>();
			for (Object inputString : inputStrings) {
				wrappedStrings.add(wrapper + inputString + wrapper);
			}

			return StringUtils.join(wrappedStrings, separator);
		} else {
			return "";
		}
	}

}