package pl.pwr.edu.parser.domain;

/**
 * Created by Jakub on 01.04.2017.
 */
public class Quote {

	private String description;
	private String body;

	public String getDescription() {
		return description;
	}

	public Quote setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getBody() {
		return body;
	}

	public Quote setBody(String body) {
		this.body = body;
		return this;
	}

	@Override
	public String toString() {
		return "Quote{" +
				"description='" + description + '\'' +
				", body='" + body + '\'' +
				'}';
	}
}
