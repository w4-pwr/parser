package pl.pwr.edu.parser.util.xml;

import pl.pwr.edu.parser.model.Article;
import pl.pwr.edu.parser.util.FileHelper;

import javax.xml.bind.JAXB;
import java.io.File;

/**
 * Created by Jakub on 10.04.2017.
 */
public class XMLWriter {
    public static void writeArticleToFile(Article article, String directory) {
        File xml = FileHelper.createArticleFile(article, directory, "xml");

        JAXB.marshal(article, xml);
    }
}
