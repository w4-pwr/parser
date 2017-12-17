package pl.pwr.edu.parser.shell.arguments.provider;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

/**
 * @author Jakub Pomykala on 12/17/17.
 * @project parser
 */
public class CharsetValueProvider implements ValueProvider {

	@Override
	public boolean supports(MethodParameter methodParameter, CompletionContext completionContext) {
		return true;
	}

	@Override
	public List<CompletionProposal> complete(
			MethodParameter methodParameter,
			CompletionContext completionContext,
			String[] strings) {
		return Charset.availableCharsets().values()
				.stream()
				.map(Charset::displayName)
				.map(CompletionProposal::new)
				.collect(Collectors.toList());
	}
}
