package pl.pwr.edu.parser.writer.path;

import pl.pwr.edu.parser.domain.Article;

/**
 * @author Jakub Pomykala on 12/1/17.
 * @project parser
 */
public final class MongoPathResolver implements PathResolver {

	@Override
	public String resolveRelativePath(Article article) {
		return "articles";
	}

	@Override
	public String resolveFileName(Article article) {
		return article.getTitle();
	}
}
