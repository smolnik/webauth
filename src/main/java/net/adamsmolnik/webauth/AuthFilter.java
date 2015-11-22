package net.adamsmolnik.webauth;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import net.adamsmolnik.model.User;

/**
 * @author asmolnik
 *
 */
@WebFilter("/secure/*")
public class AuthFilter implements Filter {

	private final NetHttpTransport transport = new NetHttpTransport();

	private final GsonFactory jsonFactory = new GsonFactory();

	private final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
			.setAudience(Arrays.asList("xyzToComplete.apps.googleusercontent.com")).build();

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String idTokenString = request.getParameter("token");
		if (idTokenString == null) {
			sendUnauthorized(response);
			return;
		}

		final GoogleIdToken idToken;
		try {
			idToken = verifier.verify(idTokenString);
			if (idToken == null) {
				sendUnauthorized(response);
			}

		} catch (GeneralSecurityException e) {
			sendUnauthorized(response);
			return;
		}
		Payload payload = idToken.getPayload();
		User user = new User((String) payload.get("name"), payload.getEmail());
		request.setAttribute(User.class.getName(), user);
		chain.doFilter(request, response);
	}

	private void sendUnauthorized(ServletResponse response) throws IOException {
		((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	public void init(FilterConfig fConfig) throws ServletException {
	}

	public void destroy() {
	}

}
