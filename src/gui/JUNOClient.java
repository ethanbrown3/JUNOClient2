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
			case ("chat"):
				handleChat(json);
				break;

			case ("whois"):
				handleWhois(json);
				break;

			case ("application"):
				handleApplication(json);
				break;

			case ("error"):
				gui.printToChat(json.getString("message"));
				break;

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
			case ("reset"):
				gui.resetGamePanel();
				break;

			}
		}
		if (message.has("action")) {
			String action = message.getString("action");
			switch (action) {
			case ("playCard"):
				gui.handlePlayCard(message);
				break;

			case ("startCard"):
				if (message.has("players")) {
					JSONArray playersMessage = message.getJSONArray("players");
					for (Object p : playersMessage) {
						if (p instanceof JSONObject) {
							JSONObject player = (JSONObject) p;
							int numOfCards = (int) player.get("cards");
							for (int i = 0; i < numOfCards; i++) {
								gui.handleCardDealt(player.getString("username"));
							}
						}
					}
					gui.printToChat("it's " + message.getString("turn") + "'s turn");
				}
				JSONObject cardMessage = new JSONObject(message.getString("card"));
				Card.Value value = Card.Value.valueOf(cardMessage.getString("value"));
				Card.Color color = Card.Color.valueOf(cardMessage.getString("color"));
				Card card = new Card(color, value);
				gui.updateDiscardPile(card);
				break;

			case ("cardDealt"):
				String player = message.getString("user");
				if (!player.equals(this.username)) {
					gui.handleCardDealt(player);
				}
				break;

			case ("turn"):
				String whosTurn = message.getString("user");
				if (whosTurn.equals(username)) {
					gui.printToChat("it's your turn");
				} else {
					gui.printToChat("it's " + whosTurn + "'s turn");
				}
				gui.setTurn(message);
				break;

			case ("callUno"):
				String unoCaller = message.getString("user");
				if (unoCaller.equals(username)) {
					gui.printToChat("You called Uno");
				} else {
					gui.printToChat(unoCaller + " called Uno");
				}
				break;

			case ("win"):
				String winner = message.getString("username");
				if (winner.equals(username)) {
					gui.printToChat("You Win!");
				} else {
					gui.printToChat(winner + " Won! Better luck next time, noob!");
				}
				break;

			case ("quit"):
				String quitter = message.getString("username");
				if (quitter.equals(username)) {
					gui.printToChat("You Quit");
					gui.resetGamePanel();
				} else {
					gui.printToChat(quitter + " Quit the game");
				}
				break;
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
