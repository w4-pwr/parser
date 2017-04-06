package pl.pwr.edu.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import pl.pwr.edu.parser.chain.ParserChain;

public class Main {

    public static void main(String[] args) throws JsonProcessingException {
        ParserChain parserChain = new ParserChain();
        parserChain.fire();
    }
}
