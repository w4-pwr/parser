package pl.pwr.edu.parser;

import pl.pwr.edu.parser.chain.ParserChain;

public class Main {
    public static void main(String[] args) {
        ParserChain parserChain = new ParserChain();
        parserChain.fire();
    }
}
