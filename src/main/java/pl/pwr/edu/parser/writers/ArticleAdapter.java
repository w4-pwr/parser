package pl.pwr.edu.parser.writers;

import java.util.Optional;
import pl.pwr.edu.parser.domain.Article;

/**
 * @author Jakub Pomykala on 11/30/17.
 */
public final class ArticleAdapter {

	private final Article article;

	private ArticleAdapter(Article article) {
		this.article = article;
	}

	public static ArticleAdapter createAdapter(Article article) {
		return new ArticleAdapter(article);
	}

	public String getTitleWithoutSpaces() {
		String articleTitle = Optional.ofNullable(article.getTitle()).orElse("no title");
		return articleTitle.replaceAll("\\s+", "-");
	}

}
