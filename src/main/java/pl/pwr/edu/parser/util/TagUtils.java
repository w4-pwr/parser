package pl.pwr.edu.parser.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import java.util.stream.Collectors;

/**
 * Created by evelan on 23/04/2017.
 */
public class TagUtils {

  public static String getTrimedAndCommaSeparatedTags(String tags) {
    return Splitter.on(CharMatcher.is(','))
        .trimResults()
        .omitEmptyStrings()
        .splitToList(tags)
        .stream()
        .distinct()
        .collect(Collectors.joining(","));
  }
}
