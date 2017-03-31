package pl.pwr.edu.parser.chain;

import pl.pwr.edu.parser.feed.RacjonalistaStep;
import pl.pwr.edu.parser.feed.Step;
import pl.pwr.edu.parser.model.Article;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParserChain {

    private List<Step> parsingSteps;
    private List<Article> articles;

    public ParserChain() {
        articles = new ArrayList<>();
        parsingSteps = new ArrayList<>();
        parsingSteps.add(new RacjonalistaStep());
    }

    public void fire() {
        articles = parsingSteps.stream().map(Step::parse).flatMap(List::stream).collect(Collectors.toList());
    }

    public List<Article> getArticles() {
        return articles;
    }
}
