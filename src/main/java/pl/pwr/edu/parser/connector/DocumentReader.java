package pl.pwr.edu.parser.connector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by matio on 10.04.2017.
 */
public class DocumentReader {

    private static Random random = new Random();

    public static Document getDocument(String url, int maxAttempts, int sleepTime) {

        return IntStream.range(0, maxAttempts)
                .mapToObj(n -> connect(url, sleepTime))
                .filter(d -> d != null)
                .findFirst()
                .orElse(null);

    }

    private static Document connect(String url, int sleepTime) {
        try {
            return Jsoup.connect(url).userAgent("Mozilla/5.0").get();
        } catch (IOException e) {
            sleepThread(sleepTime);
            return null;
        }
    }


    private static void sleepThread(int sleepTime) {
        try {
            Thread.sleep(random.nextInt(500) + sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

}
