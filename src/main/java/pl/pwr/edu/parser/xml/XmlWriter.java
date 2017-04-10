package pl.pwr.edu.parser.xml;

import pl.pwr.edu.parser.model.Article;

import javax.xml.bind.JAXB;
import java.io.File;
import java.util.Random;

/**
 * Created by matio on 10.04.2017.
 */
public class XmlWriter {
    private static Random rand = new Random();
    public static void writeArticleToFile(String directory, Article article){
        File dir = new File(directory);
        dir.mkdir();
        JAXB.marshal(article,
                dir.getAbsolutePath() + "\\" + article.hashCode() + ".xml");
    }

    private static void sleepThread(int sleepTime) {
        try {
            Thread.sleep(rand.nextInt(500) + sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
