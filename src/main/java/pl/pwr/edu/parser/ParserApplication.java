package pl.pwr.edu.parser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "pl.pwr.edu")
@EnableMongoRepositories(basePackages = "pl.pwr.edu")
public class ParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParserApplication.class, args);
	}

	@Bean
	public ExecutorService executorService() {
		return Executors.newFixedThreadPool(5);
	}
}
