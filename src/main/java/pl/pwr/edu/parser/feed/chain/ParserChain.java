package pl.pwr.edu.parser.feed.chain;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.pwr.edu.parser.feed.ParserTemplateStep;
import pl.pwr.edu.parser.writer.ArticleWriter;
import pl.pwr.edu.parser.writer.JsonWriter;

@Component
public class ParserChain {

	private final List<ParserTemplateStep> parsingSteps;

	@Autowired
	public ParserChain(List<ParserTemplateStep> parsingSteps) {
		this.parsingSteps = parsingSteps;
	}

	public void invoke(ArticleWriter articleWriterArgument) {
		ArticleWriter articleWriter = Optional
				.ofNullable(articleWriterArgument)
				.orElseGet(this::getDefaultArticleWriter);

		parsingSteps.forEach(parserTemplateStep -> parserTemplateStep.setArticleWriter(articleWriter));
		parsingSteps.forEach(ParserTemplateStep::parse);
	}

	private ArticleWriter getDefaultArticleWriter() {
		System.err.printf("Article writer not set, using default %s", JsonWriter.class.getName());
		String writePath = System.getProperty("user.dir");
		return JsonWriter.getInstance(writePath);
	}
}
