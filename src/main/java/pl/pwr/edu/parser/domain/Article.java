package pl.pwr.edu.parser.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Article {

	@Id
	private ObjectId id;

	private String source;
	private String title;
	private String body;
	private HashMap<String, String> metadata = new HashMap<>();

	@Version
	private Long version;

	public Article(String url) {
		this.source = url;
	}

	public Article setTitle(String title) {
		this.title = title;
		return this;
	}

	public Article setBody(String body) {
		this.body = body;
		return this;
	}

	public HashMap<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(HashMap<String, String> metadata) {
		this.metadata = metadata;
	}

	public List<Quote> getQuotes() {
		//Nie poczeba
		return Collections.emptyList();
	}

	public void setQuotes(List<Quote> quotes) {
		//Tego też nie czeba
	}

	public String getSource() {
		return source;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}
}
