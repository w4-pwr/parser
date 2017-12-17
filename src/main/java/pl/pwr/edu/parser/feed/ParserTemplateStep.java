package pl.pwr.edu.parser.feed;

import java.io.IOException;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.writer.ArticleWriter;

public abstract class ParserTemplateStep {

	private ArticleWriter articleWriter;

	public abstract void parse();

	public void setArticleWriter(ArticleWriter articleWriter) {
		this.articleWriter = articleWriter;
	}

	void writeArticle(Article article) {
		System.out.printf("Writing %s\n", article);
		try {
			articleWriter.write(article);
		} catch (IOException ignored) {
			System.out.println("Fetching more... ¯\\_ಠ ͟ʖಠ_/¯");
		}
	}
}
