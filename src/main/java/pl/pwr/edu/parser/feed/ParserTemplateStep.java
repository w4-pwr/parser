package pl.pwr.edu.parser.feed;

import java.io.IOException;
import java.util.List;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.writer.ArticleWriter;

public abstract class ParserTemplateStep {

	private ArticleWriter articleWriter;

	public abstract List<Article> parse();

	public void setArticleWriter(ArticleWriter articleWriter) {
		this.articleWriter = articleWriter;
	}

	void writeArticle(Article article) {
		try {
			articleWriter.write(article);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
