package pl.pwr.edu.parser.writer.path;

import pl.pwr.edu.parser.domain.Article;

/**
 * @author Jakub Pomykala on 12/1/17.
 * @project parser
 */
public interface PathResolver {

	String resolvePath(Article article);

}
