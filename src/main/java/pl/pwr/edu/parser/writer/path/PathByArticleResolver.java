package pl.pwr.edu.parser.writer.path;

import java.util.Optional;
import java.util.UUID;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.domain.ArticleAdapter;

/**
 * @author Jakub Pomykala on 12/1/17.
 * @project parser
 */
public final class PathByArticleResolver implements PathResolver {

	@Override
	public String resolvePath(Article article) {
		return Optional.ofNullable(article)
				.map(ArticleAdapter::of)
				.map(ArticleAdapter::getCleanTitle)
				.orElseGet(this::getDefaultName);
	}

	private String getDefaultName() {
		String randomUUID = UUID.randomUUID().toString();
		String NO_TITLE_PREFIX = "no-title-";
		return NO_TITLE_PREFIX + randomUUID;
	}
}
