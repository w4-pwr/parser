package pl.pwr.edu.parser.chain;

import com.google.common.collect.Lists;
import java.util.List;
import pl.pwr.edu.parser.feed.Wolnosc24Step;
import pl.pwr.edu.parser.feed.FocusStep;
import pl.pwr.edu.parser.feed.MoneyStep;
import pl.pwr.edu.parser.feed.NaTematStep;
import pl.pwr.edu.parser.feed.PrawicaStep;
import pl.pwr.edu.parser.feed.PurePcStep;
import pl.pwr.edu.parser.feed.RacjonalistaStep;
import pl.pwr.edu.parser.feed.Step;


public class ParserChain {

  private List<Step> parsingSteps;

  public ParserChain() {
    parsingSteps = Lists
        .newArrayList(
//            new NaTematStep(),
//            new RacjonalistaStep(),
//            new PrawicaStep(),
//            new MoneyStep(),
//            new FocusStep(),
//            new PurePcStep(),
            new Wolnosc24Step());
  }

  public static void main(String[] args) {
    ParserChain parserChain = new ParserChain();
    parserChain.fire();
  }

  public void fire() {
    parsingSteps.forEach(Step::parse);
  }
}
