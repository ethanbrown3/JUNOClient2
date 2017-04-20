/**
 * JUNOClient
 * @author Ethan Brown
 * CS 3230
 * Apr 12, 2017
 */
package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONObject;

import gui.Card.Value;
import junoServer.Protocol;
import junoServer.Receivable;

/**
 * @author Ethan
 *
 */
public class JUNOClient implements Receivable {

	private static final long serialVersionUID = -2227017723330822281L;

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

			}
		}
		if (json.has("action")) {
			String action = json.getString("action");
			if (action.equals("dealCard")) {
				handleDealCard(json);
			}

		}

	}

	private void handleApplication(JSONObject m) {
		JSONObject json = m;
		JSONObject message = json.getJSONObject("message");
		if (message.has("type")) {
			if (message.getString("type").equals("reset")) {
				System.out.println("reset recieved");
				gui.resetGame();
			}
		}
		if (message.has("action")) {
			String action = message.getString("action");
			switch (action) {
			case ("dealCard"): {
				handleDealCard(json);
			}

			}
		}

	}

	private void handleChat(JSONObject m) {
		JSONObject message = m;
		System.out.println(message.toString());
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

	private void handleDealCard(JSONObject m) {
		JSONObject message = new JSONObject(m.getString("card"));
		Card.Value value = Card.Value.valueOf(message.getString("value"));
		Card.Color color = Card.Color.valueOf(message.getString("color"));
		Card card = new Card(color, value);
		card.addActionListener(e -> playCard(value.toString(), color.toString()));
		gui.placeCard(card, username);

	}

	private void playCard(String val, String col) {
		JSONObject cardMessage = new JSONObject();
		cardMessage.put("color", col);
		cardMessage.put("value", val);
		JSONObject action = new JSONObject();
		action.put("action", "playCard");
		action.put("card", cardMessage);
		action.put("module", "juno");
		JSONObject message = new JSONObject();
		message.put("type", "application");
		message.put("message", action);
		protocol.sendMessage(message);
		System.out.println(message);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JUNOClient client = new JUNOClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
