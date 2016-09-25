package net.adamsmolnik.model;

/**
 * @author asmolnik
 *
 */
public class User {

	public final String name;

	public final String email;

	public final String token;

	public User(String name, String email, String token) {
		this.name = name;
		this.email = email;
		this.token = token;
	}

}
