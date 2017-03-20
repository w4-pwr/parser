package pl.pwr.edu.parser.chain;

import pl.pwr.edu.parser.feed.RacjonalistaStep;
import pl.pwr.edu.parser.feed.Step;

import java.util.ArrayList;
import java.util.List;

public class ParserChain {

    private List<Step> parsingSteps;

    public ParserChain() {
        parsingSteps = new ArrayList<>();
        parsingSteps.add(new RacjonalistaStep());
    }

    public void fire() {
        parsingSteps.forEach(Step::parse);
    }
}
