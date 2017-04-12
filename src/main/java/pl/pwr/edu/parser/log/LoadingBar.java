package pl.pwr.edu.parser.log;

import java.util.stream.IntStream;

/**
 * Created by matio on 10.04.2017.
 */
public class LoadingBar {
    private static final String VERTICAL_SIGN = "_";
    private int horizontalMaxNumber;
    private int accuracy = 1;

    public void createVerticalLoadingBar(int number) {
        System.out.println("\nNumber to load : " + number + " \n start loading :");
        IntStream.range(0, number).forEach(i -> System.out.print(VERTICAL_SIGN));
        System.out.println();
    }

    public void indicateVerticalLoading() {
        System.out.print(VERTICAL_SIGN);
    }


    public void indicateHorizontalLoading(int number) {
        if (number % accuracy == 0)
            System.out.println(number + "/" + horizontalMaxNumber);
    }

    public void setHorizontalMaxNumber(int horizontalMaxNumber) {
        System.out.println("\n number of pages to parse : " + horizontalMaxNumber);
        this.horizontalMaxNumber = horizontalMaxNumber;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }
}
