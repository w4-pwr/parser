package pl.pwr.edu.parser.feed;

import java.util.List;
import pl.pwr.edu.parser.domain.Article;

public interface Step {

	List<Article> parse();
}
