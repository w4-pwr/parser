package pl.pwr.edu.parser.feed.chain;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import pl.pwr.edu.parser.feed.ParserTemplateStep;
import pl.pwr.edu.parser.writer.ArticleWriter;
import pl.pwr.edu.parser.writer.JsonWriter;

@Component
public class ParserChain {

	private final List<ParserTemplateStep> parsingSteps;

	@Autowired
	private final ExecutorService executorService;

	@Autowired
	public ParserChain(List<ParserTemplateStep> parsingSteps, ExecutorService executorService) {
		this.parsingSteps = parsingSteps;
		this.executorService = executorService;
	}

	public void invoke(ArticleWriter articleWriterArgument) {
		ArticleWriter articleWriter = Optional
				.ofNullable(articleWriterArgument)
				.orElseGet(this::getDefaultArticleWriter);

		parsingSteps.sort(AnnotationAwareOrderComparator.INSTANCE);
		parsingSteps.forEach(parserTemplateStep -> parserTemplateStep.setArticleWriter(articleWriter));

		System.out.println("Parsers in chain:");
		parsingSteps.stream()
				.map(ParserTemplateStep::getClass)
				.map(Class::getSimpleName)
				.forEach(System.out::println);

		System.out.println("Starting...");
		parsingSteps.stream()
				.peek(this::logParserName)
				.forEach(parserTemplateStep -> executorService.submit(parserTemplateStep::parse));
	}

	private ParserTemplateStep logParserName(ParserTemplateStep parserTemplateStep) {
		String className = parserTemplateStep.getClass().getSimpleName();
		System.out.println("Running: " + className);
		return parserTemplateStep;
	}

	private ArticleWriter getDefaultArticleWriter() {
		System.err.printf("Article writer not set, using default %s", JsonWriter.class.getName());
		String writePath = System.getProperty("user.dir");
		return JsonWriter.getInstance(writePath);
	}
}
