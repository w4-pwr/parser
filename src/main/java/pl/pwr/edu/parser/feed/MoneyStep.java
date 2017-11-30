package pl.pwr.edu.parser.feed;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.pwr.edu.parser.domain.Article;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

//@Component
public class MoneyStep implements Step {

	private static final String AUTHORS_URL = "http://www.money.pl/archiwum/autor/";

	@Override
	public List<Article> parse() {
		List<String> allArticlesLinks = getAllArticlesLinks();
		List<Article> articles = allArticlesLinks
				.stream()
				.map(this::tryParseArticle)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
		articles.forEach(this::writeArticle);
		return newArrayList();
	}

	private Optional<Article> tryParseArticle(String articleUrl) {
		try {
			Article article = parseArticle(articleUrl);
			System.out.println(article);
			return Optional.of(article);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException | IndexOutOfBoundsException e) {
			System.out.printf("Article didn't match general format Url:%s\n", articleUrl);
		}
		return Optional.empty();
	}

	private void writeArticle(Article article) {
		throw new NotImplementedException();
	}

	private Article parseArticle(String articleUrl) throws IOException {
		Article article = new Article(articleUrl);
		Document doc = Jsoup.connect(articleUrl).get();
		article.setTitle(parseTitle(doc));
		article.setMetadata(parseMetaData(doc));
		article.setBody(parseBody(doc));
		return article;
	}

	private String parseTitle(Document doc) {
		return doc.getElementsByClass("article__title").first().text();
	}

	private HashMap<String, String> parseMetaData(Document doc) {
		HashMap<String, String> metaData = new HashMap<>();

		metaData.put("author", parseAuthor(doc));
		metaData.put("date", getDate(doc));
		metaData.put("category", getCategory(doc));
		metaData.put("tags", doc.getElementsByClass("tags").text());

		return metaData;
	}

	private String parseAuthor(Document doc) {
		return doc.getElementsByClass("author-name").text();
	}

	private String getDate(Document doc) {
		return doc.getElementsByClass("post-time").text();
	}

	private String getCategory(Document doc) {
		return doc.getElementsByAttributeValue("itemprop", "title").get(1).text();
	}

	private String parseBody(Document doc) {
		Element articleContent = doc.getElementsByClass("article__content").first();
		articleContent.select(".like-us").remove();
		articleContent.select(".label").remove();
		articleContent.getElementsByClass("tb02 takze").remove();
		return articleContent.text();

	}

	private List<String> getAllArticlesLinks() {
		List<String> allArticlesLinks = new ArrayList<>();

		try {
			List<String> allAutorsArticlesLinks = getAllAutorsArticlesLinks();

			for (String singleAutor : allAutorsArticlesLinks) {
				allArticlesLinks.addAll(getLinksForSingleAuthor(singleAutor));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return allArticlesLinks;
	}

	private List<String> getAllAutorsArticlesLinks() {
		List<String> autorsLinks = new ArrayList<>();
		try {
			Document document = Jsoup.connect(AUTHORS_URL).get();

			List<String> alphabeticLinks = getAllAlphabeticLinks(document);
			alphabeticLinks.add(AUTHORS_URL);
			for (String link : alphabeticLinks) {
				Document singleLetterPage = Jsoup.parse(new URL(link).openStream(), "ISO-8859-1", link);
				autorsLinks.addAll(getAutorsLinksFromSingleLetterPage(singleLetterPage));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return autorsLinks;
	}

	private List<String> getLinksForSingleAuthor(String singleAutor) throws IOException {
		Document singleAutorPage = Jsoup.connect(singleAutor).get();
		List<String> linksForSingleAuthorPage = getLinksForSingleAuthorPage(singleAutorPage);
		System.out.println(linksForSingleAuthorPage);
		return linksForSingleAuthorPage;
	}

	private List<String> getAllAlphabeticLinks(Document document) {
		return document.getElementsByClass("alfa")
				.first()
				.select("a")
				.parallelStream()
				.map(link -> link.attr("href"))
				.skip(1)
				.map(link -> AUTHORS_URL + link)
				.collect(toList());
	}

	private List<String> getAutorsLinksFromSingleLetterPage(Document document) {
		return document.getElementsByClass("lista li_3").first()
				.select("li a")
				.parallelStream()
				.map(link -> link.attr("href"))
				.map(link -> AUTHORS_URL + link)
				.collect(toList());
	}

	private List<String> getLinksForSingleAuthorPage(Document document) {
		Element articlesList = document.getElementsByClass("lista_art").first();
		if (articlesList != null) {
			return articlesList
					.getElementsByClass("seo_link_src")
					.parallelStream()
					.map(tag -> tag.attr("href"))
					.filter(link -> link.contains("money.pl"))
					.collect(toList());
		} else {
			System.out.printf("There are no articles for this author:%s", document.location());
		}
		return newArrayList();
	}
}
