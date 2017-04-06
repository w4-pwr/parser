package pl.pwr.edu.parser.feed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.model.Quote;
import pl.pwr.edu.parser.model.RacjonalistaArticle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RacjonalistaStep implements Step {
    private final static String baseUrl = "http://www.racjonalista.pl";
    private final static String articleListUrl = "http://www.racjonalista.pl/index.php/s,27";
    private final static Pattern FOOTNOTE_PATTERN = Pattern.compile("\\[\\s*(\\d+)\\s*]");
    private static Random rand = new Random();

    @Override
    public List<Article> parse() {
        List<Article> articles = new ArrayList<>();

        getArticlesLinks().forEach(link -> articles.add(parseLink(link)));

        return articles;
    }

    private List<String> getArticlesLinks() {
        Document doc = connect(articleListUrl);

        return doc.select("#oTxt").first()
                .select(".linkart")
                .stream()
                .map(link -> link.attr("href"))
                .collect(Collectors.toList());

    }

    private Article parseLink(String articleUrl) {
        RacjonalistaArticle article = new RacjonalistaArticle();
        Document doc = connect(baseUrl + articleUrl);

        article.setTitle(doc.select("meta[property=og:title]").attr("content"));
        parseArticleMetaData(article, doc);

        article.setBody(joinArticlePages(article, doc));
        return article;
    }

    private void parseArticleMetaData(RacjonalistaArticle article, Document doc) {
        Element category = doc.select(".linkdzial").last();
        article.getMetadata().put("author", doc.select("a[rel=author]").text().trim());
        article.getMetadata().put("category", category.getElementsByTag("b").text().trim());
        article.getMetadata().put("date", Jsoup.parse(category.nextSibling().nextSibling().toString()).text().trim());
        article.getMetadata().put("keywords", doc.select("meta[name=keywords]").attr("content").trim());
        article.getMetadata().put("description", doc.select("meta[name=description]").attr("content").trim());
    }

    private String joinArticlePages(RacjonalistaArticle article, Document doc) {
        return String.join("\n", parseArticleBody(doc, null, article.getQuotes()));
    }

    private List<String> parseArticleBody(Document doc, List<String> pages, List<Quote> quotes) {
        if (pages == null)
            pages = new ArrayList<>();

        Elements pageText = doc.select("#oTxt").first().select("p");
        quotes.addAll(extractQuotes(doc));
        pages.add(pageText.text().trim());

        parseLeftPages(doc, pages, quotes);
        return pages;
    }

    /**
     * Finds quotes, matches them with their description and removes them from article body
     **/
    private List<Quote> extractQuotes(Document doc) {
        List<Quote> res = new ArrayList<>();
        Elements quotes = doc.select(".cytat,blockquote,.przyp");
        Elements descriptions = doc.select(".fn>span");

        quotes.forEach(quote -> parseQuote(quote, descriptions, res));
        quotes.remove();

        return res;
    }

    /**
     * Find quote description, parse its body and remove empty entries
     */
    private void parseQuote(Element quote, Elements descriptions, List<Quote> quoteList) {
        Quote q = new Quote();

        String body = handleFootNoteNumber(quote, descriptions, q);
        if (quote.hasClass("przyp")) {
            body = findTextBeforeFootnoteMarks(quote);
            if (body == null)
                return;
        }

        q.setBody(body);
        quoteList.add(q);
    }

    /**
     * If footNoteNumber is found, find quote description and remove [ x ] from article body
     */
    @NotNull
    private String handleFootNoteNumber(Element quote, Elements descriptions, Quote q) {
        Matcher matcher = FOOTNOTE_PATTERN.matcher(quote.text());

        if (matcher.find()) {
            q.setDescription(findQuoteDescription(descriptions, matcher));
            return quote.text().replaceAll(FOOTNOTE_PATTERN.pattern(), "").trim();
        } else {
            q.setDescription("");
            return quote.text().trim();
        }
    }

    private String findQuoteDescription(Elements descriptions, Matcher matcher) {
        String footnoteNumber = matcher.group(1);
        Element description = descriptions
                .stream()
                .filter(desc -> desc.attr("id").contains(footnoteNumber))
                .findFirst()
                .orElseThrow(IndexOutOfBoundsException::new);

        int index = descriptions.indexOf(description);
        return descriptions.get(index).text().trim();
    }

    /**
     * Eliminates doubled blockquotes and .cytat,
     * Finds non-empty textNode prepending element with footNoteNumber
     * Removes it from article body
     */
    @Nullable
    private String findTextBeforeFootnoteMarks(Element quote) {
        if (quote.parent().parent().tagName().equals("blockquote")
                || quote.parent().className().equals("cytat"))
            return null;

        Node node = findNonEmptyChildNode(quote);
        if (node == null) return null;
        String body = getChildNodeText(node);

        node.remove();
        return body;
    }

    @Nullable
    private Node findNonEmptyChildNode(Element quote) {
        Node node = quote.previousSibling();
        while (node.toString().trim().isEmpty()) {
            node = node.previousSibling();
            if (node == null)
                return null;
        }
        return node;
    }

    /**
     * Ex
     * If node has <tag></tag> around it, removes it
     */
    @NotNull
    private String getChildNodeText(Node node) {
        String body;
        if (node.childNodes().size() != 0)
            body = node.childNode(0).toString().trim();
        else
            body = node.toString().trim();
        return body;
    }

    private void parseLeftPages(Document doc, List<String> pages, List<Quote> quotes) {
        Element nextPageButton = doc.select(".pg").last();
        if (nextPageButton != null && nextPageButton.childNode(0) instanceof TextNode) {
            String nextPageLink = nextPageButton.attr("href");
            doc = connect(baseUrl + nextPageLink);
            parseArticleBody(doc, pages, quotes);
        }
    }

    private static Document connect(String url) {
        try {
            return Jsoup.connect(url).userAgent("Mozilla/5.0").get();
        } catch (IOException e) {
            sleepThread();
            return connect(url);
        }
    }

    private static void sleepThread() {
        try {
            Thread.sleep(rand.nextInt(500) + 3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
