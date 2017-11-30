package pl.pwr.edu.parser.feed.chain;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.pwr.edu.parser.feed.Step;

@Component
public class ParserChain {

	private final List<Step> parsingSteps;

	@Autowired
	public ParserChain(List<Step> parsingSteps) {
		this.parsingSteps = parsingSteps;
	}

	public void invoke() {
		parsingSteps.forEach(Step::parse);
	}
}
