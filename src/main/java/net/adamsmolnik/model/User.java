package net.adamsmolnik.model;

/**
 * @author asmolnik
 *
 */
public class User {

	public final String name, email, token, subject;

	public User(String name, String email, String token, String subject) {
		this.name = name;
		this.email = email;
		this.token = token;
		this.subject = subject;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", email=" + email + ", token=" + token + ", subject=" + subject + "]";
	}

}
