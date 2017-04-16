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

import junoServer.Protocol;
import junoServer.Receivable;

/**
 * @author Ethan
 *
 */
public class JUNOClient extends JFrame implements Receivable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2227017723330822281L;

	private JPanel contentPane;
	private JTextArea chatArea;
	private JTextArea chatInputArea;
	private Protocol protocol;
	private String username;
	private boolean userSet = false;

	/**
	 * 
	 */
	public JUNOClient() {
		connectToServer();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout());

		intializeGameArea();
		initializeChat();

	}

	private void connectToServer() {
		try {
			protocol = new Protocol(this);
		} catch (IOException e) {
			System.err.println("Error in setting up protocol");
			e.printStackTrace();
		}

	}

	private void intializeGameArea() {
		JPanel gamePane = new JPanel(new BorderLayout());
		JButton startButton = new JButton("Start Game");
		startButton.addActionListener(e -> startGame());
		Card cardTest = new Card(Card.Color.RED, Card.Value.ZERO);
		gamePane.add(cardTest, "Center");
		gamePane.add(startButton, "South");
		contentPane.add(gamePane, "Center");

	}

	private void initializeChat() {
		// center panel
		JPanel chatPane = new JPanel(new BorderLayout());
		chatArea = new JTextArea();
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);
		JScrollPane chatScroll = new JScrollPane(chatArea);
		chatScroll.setSize(150, 200);
		chatScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		chatPane.add(chatScroll, "Center");

		// south panel
		JPanel inputPanel = new JPanel(new FlowLayout());
		chatInputArea = new JTextArea(3, 15);
		chatInputArea.setEditable(true);
		chatInputArea.setLineWrap(true);
		chatInputArea.setWrapStyleWord(true);
		JScrollPane inputScroll = new JScrollPane(chatInputArea);
		inputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		inputPanel.add(inputScroll);
		chatInputArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiers() == KeyEvent.CTRL_MASK) {
					sendChat();
				}
			}
		});

		// send button setup
		JButton send = new JButton("Send");
		inputPanel.add(send);
		send.addActionListener(e -> sendChat());

		chatPane.add(inputPanel, "South");
		contentPane.add(chatPane, "West");
		chatPane.setVisible(true);
		chatInputArea.requestFocusInWindow();
	}

	private void startGame() {
		printToChat("Requesting New Game\n");
		JSONObject message = new JSONObject();
		message.put("type", "startGame");
		protocol.sendMessage(message);
	}

	private void sendChat() {
		JSONObject message = new JSONObject();

		String chatSend;
		chatSend = chatInputArea.getText();

		if (chatSend.equals("/whois")) {
			message.put("type", "whois");
			protocol.sendMessage(message);
			chatInputArea.setText("");
			printToChat("whois requested");
			return;
		}

		message.put("type", "chat");

		String whisperRegex = "(?<=^|(?<=[^a-zA-Z0-9-_\\\\.]))@([A-Za-z][A-Za-z0-9_]+)";
		Matcher matcher = Pattern.compile(whisperRegex).matcher(chatSend);
		if (matcher.find()) {
			System.out.println(matcher.group(0));
			StringBuilder whisperRecipient = new StringBuilder(matcher.group(0));
			whisperRecipient.deleteCharAt(0);
			System.out.println(whisperRecipient);
			message.put("username", whisperRecipient);
			chatSend += " (Whispered from " + username + ")";
		}

		message.put("message", chatSend);

		System.out.println("executed sendText()");

		protocol.sendMessage(message);
		printToChat(username + ": " + chatSend);
		chatInputArea.setText("");
	}

	private void handleChat(JSONObject m) {
		JSONObject message = m;
		System.out.println(message.toString());
		printToChat(message.getString("fromUser") + ": " + message.getString("message"));
	}

	private void handleWhois(JSONObject m) {
		JSONArray usernames = m.getJSONObject("message").getJSONArray("users");
		if (usernames.length() == 1) {
			printToChat("ALLL BY MYSEEEELF! you're the only one online... loser");
		}
		printToChat("Users Currently Online:");
		for (int i = 0; i < usernames.length(); i++) {
			printToChat((i + 1) + ": " + usernames.getJSONObject(i).get("username").toString());
		}

	}

	public void printToChat(String chat) {
		chatArea.append(chat + "\n");
		chatArea.setCaretPosition(chatArea.getDocument().getLength());

	}

	@Override
	public void setUsername(String user) {
		if (!userSet) {
			this.username = user;
		}
	}

	@Override
	public void giveMessage(JSONObject m) {
		JSONObject message = m;
		System.out.println(m.toString());
		String type = message.getString("type");
		switch (type) {
		case ("chat"): {
			handleChat(message);
			break;
		}
		case ("whois"): {
			handleWhois(message);
			break;
		}
		case ("application"): {
			handleApplication(message);
		}
		}
	}

	private void handleApplication(JSONObject message) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JUNOClient client = new JUNOClient();
					client.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
