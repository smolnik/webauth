package net.adamsmolnik.webauth;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.WebIdentityFederationSessionCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;

import net.adamsmolnik.model.User;

/**
 * @author asmolnik
 *
 */

@WebServlet("/secure/getData")
public class GetDataServlet extends HttpServlet {

	private static final long serialVersionUID = 5923587435217952548L;

	private final Map<String, Set<String>> accessKeyHistoryMap = new ConcurrentHashMap<>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}

	private void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User user = (User) req.getSession().getAttribute(User.class.getName());
		String userName = user.name;
		System.out.println(user.token);
		WebIdentityFederationSessionCredentialsProvider webProvider = new WebIdentityFederationSessionCredentialsProvider(user.token, null,
				"arn:aws:iam::123456789:role/auth-test-db").withSessionDuration(900);
		AWSSessionCredentials cr = webProvider.getCredentials();
		accessKeyHistoryMap.putIfAbsent(userName, new HashSet<>());
		accessKeyHistoryMap.get(userName).add(cr.getAWSAccessKeyId());
		DynamoDB db = new DynamoDB(new AmazonDynamoDBClient(cr));
		ItemCollection<QueryOutcome> ic = db.getTable("auth-test").query("user-email", user.email);
		String keyNames = StreamSupport.stream(ic.spliterator(), false).map(o -> o.getString("key-name")).collect(Collectors.joining(" "));
		resp.setContentType("text/plain");
		resp.getWriter().println("key names of " + userName + ": " + keyNames);
		resp.getWriter().println("acccess key history: " + accessKeyHistoryMap.get(userName));
	}

}
