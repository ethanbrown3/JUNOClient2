package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;

import junoServer.Protocol;

public class JunoGUI extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3617439726342648090L;
	private JPanel contentPane;
	private JTextArea chatArea;
	private JTextArea chatInputArea;
	private Protocol protocol;
	private String username;
	private boolean gameStarted = false;
	private JPanel gamePane;
	private JPanel hand1, hand2, hand3, hand4;

	public JunoGUI(Protocol protocol, String username) {
		this.protocol = protocol;
		this.username = username;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout());

		intializeGameArea();
		initializeChat();

	}

	private void intializeGameArea() {
		gamePane = new JPanel(new BorderLayout());
		contentPane.add(gamePane, "Center");
		hand1 = new JPanel(new FlowLayout());
		JScrollPane scrollPane1 = new JScrollPane(hand1, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		gamePane.add(scrollPane1, "South");
		hand2 = new JPanel(new FlowLayout());
		JScrollPane scrollPane2 = new JScrollPane(hand2, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		hand3 = new JPanel(new FlowLayout());
		gamePane.add(scrollPane2, "North");
		JScrollPane scrollPane3 = new JScrollPane(hand3, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		hand4 = new JPanel(new FlowLayout());
		gamePane.add(scrollPane3, "West");
		JScrollPane scrollPane4 = new JScrollPane(hand4, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		gamePane.add(scrollPane4, "East");
		JPanel gameControl = new JPanel(new FlowLayout());
		// start game button
		JButton startButton = new JButton("Start Game");
		startButton.addActionListener(e -> startGame());
		gameControl.add(startButton);
		// reset game button
		JButton resetButton = new JButton("Reset Game");
		resetButton.addActionListener(e -> resetGame());
		gameControl.add(resetButton);
		
		gamePane.add(gameControl, "North");

	}

	private void resetGame() {
		JSONObject message = new JSONObject();
		message.put("type", "application");
		JSONObject action = new JSONObject();
		action.put("action", "reset");
		message.put("message", action);
		System.out.println(message);
		protocol.sendMessage(message);

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
		chatScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		chatPane.add(chatScroll, "Center");

		// south panel
		JPanel inputPanel = new JPanel(new FlowLayout());
		chatInputArea = new JTextArea(3, 15);
		chatInputArea.setEditable(true);
		chatInputArea.setLineWrap(true);
		chatInputArea.setWrapStyleWord(true);
		JScrollPane inputScroll = new JScrollPane(chatInputArea);
		inputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
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

	private void sendChat() {
		JSONObject message = new JSONObject();

		String chatSend;
		chatSend = chatInputArea.getText();

		// check for whois message
		if (chatSend.equals("/whois")) {
			message.put("type", "whois");
			protocol.sendMessage(message);
			chatInputArea.setText("");
			printToChat("whois requested");
			return;
		}
		message.put("type", "chat");

		// check for whisper @<user>
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
		protocol.sendMessage(message);
		printToChat(username + ": " + chatSend);
		chatInputArea.setText("");
	}

	private void startGame() {
		printToChat("Requesting New Game\n");
		JSONObject message = new JSONObject();
		message.put("type", "application");
		JSONObject action = new JSONObject();

		if (gameStarted) {
			action.put("action", "joinGame");
		} else {
			action.put("action", "startGame");
			gameStarted = true;
		}
		action.put("module", "juno");
		message.put("message", action);
		protocol.sendMessage(message);

	}

	public void printToChat(String chat) {
		chatArea.append(chat + "\n");
		chatArea.setCaretPosition(chatArea.getDocument().getLength());

	}

	public void placeCard(Card c, String playerHand) {
		hand1.add(c);
		gamePane.updateUI();
	}

}
