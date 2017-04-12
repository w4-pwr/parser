package pl.pwr.edu.parser.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Random;

public class JsoupConnector {
    private static Random rand = new Random();

    public static Document connect(String url, int sleepTime) {
        try {
            return Jsoup.connect(url).userAgent("Mozilla/5.0").get();
        } catch (IOException e) {
            sleepThread(sleepTime);
            return connect(url, sleepTime);
        }
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
