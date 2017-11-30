package pl.pwr.edu.parser.writers;

import java.io.File;
import javax.xml.bind.JAXB;
import pl.pwr.edu.parser.domain.Article;
import pl.pwr.edu.parser.util.FileHelper;

/**
 * Created by Jakub on 10.04.2017.
 */
public class XMLWriter {

	public static void writeArticleToFile(Article article, String directory) {
		File xml = FileHelper.createArticleFile(article, directory, "xml");

		JAXB.marshal(article, xml);
	}
}
