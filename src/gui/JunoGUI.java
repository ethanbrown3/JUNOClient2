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
		gamePane.add(hand1, "South");
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
		JButton startButton = new JButton("Start Game");
		startButton.addActionListener(e -> startGame());
		inputPanel.add(startButton);
		chatPane.add(inputPanel, "South");
		contentPane.add(chatPane, "West");
		chatPane.setVisible(true);
		chatInputArea.requestFocusInWindow();
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

	public void printToChat(String chat) {
		chatArea.append(chat + "\n");
		chatArea.setCaretPosition(chatArea.getDocument().getLength());

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
	public void placeCard(Card c, String hand) {
		hand1.add(c);
		gamePane.updateUI();
	}
	
}
