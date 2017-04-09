package pl.pwr.edu.parser.chain;

import pl.pwr.edu.parser.feed.NaTematStep;
import pl.pwr.edu.parser.feed.PrawicaStep;
import pl.pwr.edu.parser.feed.RacjonalistaStep;
import pl.pwr.edu.parser.feed.Step;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.NaTematArticle;
import pl.pwr.edu.parser.model.PrawicaArticle;
import pl.pwr.edu.parser.model.RacjonalistaArticle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParserChain {

    private List<Step> parsingSteps;
    private List<Article> articles;

    public ParserChain() {
        articles = new ArrayList<>();
        parsingSteps = new ArrayList<>();
        parsingSteps.add(new NaTematStep());
        parsingSteps.add(new RacjonalistaStep());
        parsingSteps.add(new PrawicaStep());


    }


    public void fire() {
        articles = parsingSteps.stream().map(Step::parse).flatMap(List::stream).collect(Collectors.toList());
    }

    public List<Article> getArticles() {
        return articles;
    }
}
