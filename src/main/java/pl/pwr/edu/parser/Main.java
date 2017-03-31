package pl.pwr.edu.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.pwr.edu.parser.chain.ParserChain;

public class Main {
    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws JsonProcessingException {
        ParserChain parserChain = new ParserChain();
        parserChain.fire();

        System.out.println(mapper.writeValueAsString(parserChain.getArticles()));
    }
}
