package pl.pwr.edu.parser.util.xml;

import pl.pwr.edu.parser.model.Article;

import javax.xml.bind.JAXB;
import java.io.File;

/**
 * Created by matio on 10.04.2017.
 */
public class XMLWriter {
    public static void writeArticleToFile(String directory, Article article) {
        String author = article.getMetadata().get("author");
        String dirName = author.isEmpty() ? "Uncategorized" : author;

        File dir = new File(directory + "\\" + dirName);
        dir.mkdirs();

        JAXB.marshal(article,
                dir.getAbsolutePath() + "\\" + article.hashCode() + ".xml");
    }
}
