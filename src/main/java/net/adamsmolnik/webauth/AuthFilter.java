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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

	private final static String ATTR_USER_NAME = User.class.getName();

	private final NetHttpTransport transport = new NetHttpTransport();

	private final GsonFactory jsonFactory = new GsonFactory();

	private final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
			.setAudience(Arrays.asList("xyzToComplete.apps.googleusercontent.com")).build();

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpSession session = ((HttpServletRequest) req).getSession();
		User user = (User) session.getAttribute(ATTR_USER_NAME);
		if (user != null) {
			chain.doFilter(req, resp);
			return;
		}

		String idTokenString = req.getParameter("token");
		if (idTokenString == null || "".equals(idTokenString.trim())) {
			sendUnauthorized(resp);
			return;
		}

		final GoogleIdToken idToken;
		try {
			idToken = verifier.verify(idTokenString);
		} catch (GeneralSecurityException e) {
			sendUnauthorized(resp);
			return;
		}
		Payload payload = idToken.getPayload();
		user = new User((String) payload.get("name"), payload.getEmail(), idTokenString);
		session.setAttribute(ATTR_USER_NAME, user);
	}

	private void sendUnauthorized(ServletResponse response) throws IOException {
		((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	public void init(FilterConfig fConfig) throws ServletException {
	}

	public void destroy() {
	}

}
