package pl.pwr.edu.parser;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "pl.pwr.edu")
@EnableMongoRepositories(basePackages = "pl.pwr.edu")
public class ParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParserApplication.class, args);
	}

	private MongoClient mongoClient() {
		String DATABASE_URL = "localhost";
		ServerAddress node_1 = new ServerAddress(DATABASE_URL, 27017);
		ServerAddress node_2 = new ServerAddress(DATABASE_URL, 27027);
		ServerAddress node_3 = new ServerAddress(DATABASE_URL, 27037);
		return new MongoClient("localhost");
	}

	@Bean
	public MongoTemplate mongoTemplate() {
		return new MongoTemplate(mongoClient(), "pwr");
	}

}
