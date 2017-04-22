package pl.pwr.edu.parser.feed;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pl.pwr.edu.parser.model.Article;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

public class MoneyStep implements Step {

    public static final String AUTORS_URL = "http://www.money.pl/archiwum/autor/";

    @Override
    public List<Article> parse() {
        getAllAutorsArticlesLinks();
        return newArrayList();
    }

    private List<String> getAllAutorsArticlesLinks() {
        List<String> autorsLinks = new ArrayList<>();
        try {
            Document document = Jsoup.connect(AUTORS_URL).get();

            List<String> alphabeticLinks = getAllAlphabeticLinks(document);
            alphabeticLinks.add(AUTORS_URL);
            for (String link : alphabeticLinks) {
                Document singleLetterPage = Jsoup.connect(link).get();
                autorsLinks.addAll(getAutorsLinksFromSingleLetterPage(singleLetterPage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return autorsLinks;
    }

    private List<String> getAllAlphabeticLinks(Document document) {
        return document.getElementsByClass("alfa")
                .first()
                .select("a")
                .stream()
                .map(link -> link.attr("href"))
                .skip(1)
                .map(link -> AUTORS_URL + link)
                .collect(Collectors.toList());
    }

    private List<String> getAutorsLinksFromSingleLetterPage(Document document) {
        return document.getElementsByClass("lista li_3").first()
                .select("li a")
                .stream()
                .map(link -> link.attr("href"))
                .map(link -> AUTORS_URL + link)
                .collect(Collectors.toList());
    }
}
