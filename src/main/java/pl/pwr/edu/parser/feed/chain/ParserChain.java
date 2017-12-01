package pl.pwr.edu.parser.feed.chain;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.feed.Step;
import pl.pwr.edu.parser.writer.ArticleWriter;
import pl.pwr.edu.parser.writer.JsonWriter;

@Component
public class ParserChain {

	private final List<Step> parsingSteps;
	private ArticleWriter articleWriter;

	@Autowired
	public ParserChain(List<Step> parsingSteps) {
		this.parsingSteps = parsingSteps;
	}

	public void invoke(ArticleWriter articleWriter) {
		this.articleWriter = Optional.ofNullable(articleWriter).orElseGet(this::getDefaultArticleWriter);
		parsingSteps.stream()
				.map(Step::parse)
				.flatMap(Collection::stream)
				.forEach(this::tryWriteArticle);
		parsingSteps.forEach(Step::parse);
	}

	private ArticleWriter getDefaultArticleWriter() {
		System.err.printf("Article writer not set, using default %s", JsonWriter.class.getName());
		String writePath = System.getProperty("user.dir");
		return JsonWriter.getInstance(writePath);
	}

	private void tryWriteArticle(Article article) {
		try {
			articleWriter.write(article);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
