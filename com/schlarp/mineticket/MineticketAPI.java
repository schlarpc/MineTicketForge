package com.schlarp.mineticket;

import static argo.jdom.JsonNodeFactories.field;
import static argo.jdom.JsonNodeFactories.object;
import static argo.jdom.JsonNodeFactories.string;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

public class MineticketAPI {
	/**
	 * JSON parser instance used to parse API responses
	 */
	private static final JdomParser parser = new JdomParser();

	/**
	 * Sends key/value argument pairs to an API endpoint
	 * 
	 * @param endpoint
	 *            The endpoint name
	 * @param arguments
	 *            Map of arguments to the endpoint
	 * @return Server JSON response data
	 */
	public static JsonRootNode makeAPIRequest(String endpoint, Map<String, String> arguments) {
		try {
			// Encode parameters as form values for POST
			StringBuilder postData = new StringBuilder();
			postData.append("api_key=");
			postData.append(URLEncoder.encode(Mineticket.apiKey, "UTF-8"));
			for (Map.Entry<String, String> entry : arguments.entrySet()) {
				postData.append("&");
				postData.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				postData.append("=");
				postData.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			}
			byte[] postDataEncoded = postData.toString().getBytes("UTF-8");

			// Set up server connection
			URL url = new URL(Mineticket.mineticketServer + "/api/" + endpoint);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Length",
					"" + Integer.toString(postDataEncoded.length));

			// Write POST data to connection
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(postDataEncoded);
			wr.flush();
			wr.close();

			// Read response data
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			connection.disconnect();

			// Bug (?) in output makes some errors appear in a different format;
			// normalize from error: message to status: failed, message: message
			JsonRootNode responseJson = parser.parse(response.toString());
			if (!responseJson.isStringValue("status") && responseJson.isStringValue("error")) {
				return object(field("status", string("failed")),
						field("message", string(responseJson.getStringValue("error"))));
			}
			return parser.parse(response.toString());

		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		} catch (InvalidSyntaxException e) {
		}

		return object(field("status", string("failed")),
				field("message", string("An error occurred while communicating with MineTicket.")));
	}

	/**
	 * Adds a new user to the MineTicket system.
	 * 
	 * @param username
	 *            Minecraft username to be registered
	 * @return Server JSON response data, including username and email of user
	 *         if successful
	 */
	public static JsonRootNode registerUser(String username) {
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("username", username);

		return makeAPIRequest("registeruser", arguments);
	}

	/**
	 * Attaches a registered user to a MineTicket account.
	 * 
	 * @param username
	 *            Minecraft username to be activated
	 * @param email
	 *            Email address of MineTicket user
	 * @return Server JSON response data, including activation key if
	 *         unregistered
	 */
	public static JsonRootNode activateUser(String username, String email) {
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("username", username);
		arguments.put("email", email);

		return makeAPIRequest("activateuser", arguments);
	}

	/**
	 * Ban or unban by username, if issuer has moderator privleges.
	 * 
	 * @param username
	 *            Minecraft username of player to be banned or unbanned
	 * @param banned_by
	 *            Minecraft username of player issuing ban
	 * @param ban_state
	 *            Status to set on user ("ban" or "unban")
	 * @param ban_reason
	 *            (Optional) Reason for ban
	 * @return Server JSON response data
	 */
	public static JsonRootNode banUser(String username, String banned_by, String ban_state,
			String ban_reason) {
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("username", username);
		arguments.put("banned_by", banned_by);
		arguments.put("ban_state", ban_state);

		if (ban_reason != null) {
			arguments.put("ban_reason", ban_reason);
		}

		return makeAPIRequest("banuser", arguments);
	}

	/**
	 * Allows future commands to be polyfilled by the server.
	 * 
	 * @param username
	 *            Minecraft user name of issuing player
	 * @param command
	 *            Full command arguments, e.g. "start party" for
	 *            "/mineticket start party"
	 * @return Server JSON response data
	 */
	public static JsonRootNode unknownCommand(String username, String command) {
		HashMap<String, String> arguments = new HashMap<String, String>();
		arguments.put("username", username);
		arguments.put("command", command);

		return makeAPIRequest("unknowncommand", arguments);
	}

}
