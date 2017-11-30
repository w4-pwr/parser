package pl.pwr.edu.parser.writers;

import java.io.IOException;
import pl.pwr.edu.parser.domain.Article;

/**
 * @author Jakub Pomykala on 11/30/17.
 */
public interface ArticleWriter {

	void write(Article article) throws IOException;

}
