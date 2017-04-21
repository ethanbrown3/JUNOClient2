/**
 * JUNOClient
 * @author Ethan Brown
 * CS 3230
 * Apr 12, 2017
 */
package gui;

import java.awt.EventQueue;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import junoServer.Protocol;
import junoServer.Receivable;

/**
 * @author Ethan
 *
 */
public class JUNOClient implements Receivable {

	private Protocol protocol;
	private String username;
	private boolean userSet = false;
	private JunoGUI gui;

	public JUNOClient() {
		connectToServer();
		gui = new JunoGUI(protocol, username);
		gui.setVisible(true);
	}

	private void connectToServer() {
		try {
			protocol = new Protocol(this);
		} catch (IOException e) {
			System.err.println("Error in setting up protocol");
			e.printStackTrace();
		}
	}

	@Override
	public void setUsername(String user) {
		if (!userSet) {
			this.username = user;
		}
	}

	@Override
	public void giveMessage(JSONObject m) {
		JSONObject json = m;
		if (json.has("type")) {
			String type = json.getString("type");
			switch (type) {
			case ("chat"): {
				handleChat(json);
				break;
			}
			case ("whois"): {
				handleWhois(json);
				break;
			}
			case ("application"): {
				handleApplication(json);
				break;
			}
			case ("error"): {
				gui.printToChat(json.getString("message"));
				break;
			}
			}
		}
		if (json.has("action")) {
			String action = json.getString("action");
			if (action.equals("dealCard")) {
				gui.handleDealCard(json);
			}
		}
	}

	private void handleApplication(JSONObject m) {
		JSONObject json = m;
		JSONObject message = json.getJSONObject("message");
		if (message.has("type")) {
			String type = message.getString("type");
			switch (type) {
			case ("reset"): {
				System.out.println("reset recieved");
				gui.resetGamePanel();
				break;
			}
			}
		}
		if (message.has("action")) {
			String action = message.getString("action");
			switch (action) {
			case ("playCard"): {
				if (message.getString("user").equals(this.username)) {
					JSONObject cardMessage = new JSONObject(message.getString("card"));
					Card.Value value = Card.Value.valueOf(cardMessage.getString("value"));
					Card.Color color = Card.Color.valueOf(cardMessage.getString("color"));
					Card card = new Card(color, value);
					gui.getHandSouth().removeCard(card);
					gui.updateDiscardPile(card);
					break;
				}
			}
			case ("startCard"): {
				JSONObject cardMessage = new JSONObject(message.getString("card"));
				Card.Value value = Card.Value.valueOf(cardMessage.getString("value"));
				Card.Color color = Card.Color.valueOf(cardMessage.getString("color"));
				Card card = new Card(color, value);
				gui.updateDiscardPile(card);
				break;
			}
			case ("cardDealt"): {
				String player = message.getString("user");
				if (!player.equals(this.username)) {
					gui.handleCardDealt(player);
				}
				break;
			}
			case ("turn"): {
				gui.printToChat("it's " + message.getString("user") + "'s turn");
				break;
			}
			}
		}
	}

	private void handleChat(JSONObject m) {
		JSONObject message = m;
		System.out.println("chat message:" + message.toString());
		gui.printToChat(message.getString("fromUser") + ": " + message.getString("message"));
	}

	private void handleWhois(JSONObject m) {
		JSONArray usernames = m.getJSONObject("message").getJSONArray("users");
		if (usernames.length() == 1) {
			gui.printToChat("You're the only one online... loser");
		}
		gui.printToChat("Users Currently Online:");
		for (int i = 0; i < usernames.length(); i++) {
			String whoIsOutput = (i + 1) + ": " + usernames.getJSONObject(i).get("username").toString();
			String module = usernames.getJSONObject(i).get("modules").toString();
			if (!module.isEmpty())
				whoIsOutput += " module: " + module;

			gui.printToChat(whoIsOutput);
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new JUNOClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
