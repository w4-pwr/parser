package pl.pwr.edu.parser;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author Jakub Pomykala on 12/6/17.
 * @project parser
 */
public class MongoConfiguration {

	private MongoClient mongoClient() {
		ServerAddress node_1 = new ServerAddress("node1.example.com", 27017);
		ServerAddress node_2 = new ServerAddress("node2.example.com", 27017);
		ServerAddress node_3 = new ServerAddress("node3.example.com", 27017);
		return new MongoClient(Collections.singletonList(node_1));
	}

	@Bean
	public MongoTemplate mongoTemplate() {
		return new MongoTemplate(mongoClient(), "pwr");
	}

}
