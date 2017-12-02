package pl.pwr.edu.parser.writer;

import java.io.IOException;
import java.nio.file.Path;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.writer.path.PathResolver;

/**
 * @author Jakub Pomykala on 11/30/17.
 */
public interface ArticleWriter {

	void write(Article article) throws IOException;

	Path writeAndGetPath(Article article) throws IOException;

	void setPathResolver(PathResolver strategy);

}
