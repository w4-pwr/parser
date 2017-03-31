package pl.pwr.edu.parser.feed;

import pl.pwr.edu.parser.model.Article;

import java.util.List;

public interface Step {

    List<Article> parse();

}
