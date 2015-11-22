package net.adamsmolnik.webauth;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute(User.class.getName());
		DynamoDB db = new DynamoDB(new AmazonDynamoDBClient());
		ItemCollection<QueryOutcome> ic = db.getTable("auth-test").query("user-email", user.getEmail());
		String keyNames = StreamSupport.stream(ic.spliterator(), false).map(o -> o.getString("key-name")).collect(Collectors.joining(" "));
		resp.setContentType("text/plain");
		resp.getWriter().println(user.getName() + "' (" + user.getEmail() + ") key names : " + keyNames);
	}

}
