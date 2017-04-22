package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
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

	private static final long serialVersionUID = 3617439726342648090L;
	private JPanel contentPane;
	private JTextArea chatArea;
	private JTextArea chatInputArea;
	private Protocol protocol;
	private String username;
	private JPanel gamePane, discardPile;
	private Hand handSouth, handNorth, handWest, handEast;
	private HashMap<String, Hand> hands;

	public JunoGUI(Protocol protocol, String username) {
		this.protocol = protocol;
		this.username = username;
		this.setTitle("JUNO - " + username);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 600);
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

		// setup player hands
		handSouth = new Hand(Card.CardOrientation.UP);
		JScrollPane scrollSouth = new JScrollPane(handSouth, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		gamePane.add(scrollSouth, "South");
		hands = new HashMap<>();
		hands.put(username, handSouth);

		handNorth = new Hand(Card.CardOrientation.UP);
		handNorth.setLayout(new BoxLayout(handNorth, BoxLayout.LINE_AXIS));
		JScrollPane scrollNorth = new JScrollPane(handNorth, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		gamePane.add(scrollNorth, "North");
		
		handWest = new Hand(Card.CardOrientation.RIGHT);
		handWest.setLayout(new BoxLayout(handWest, BoxLayout.PAGE_AXIS));
		JScrollPane scrollWest = new JScrollPane(handWest, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		gamePane.add(scrollWest, "West");
		
		handEast = new Hand(Card.CardOrientation.LEFT);
		handEast.setLayout(new BoxLayout(handEast, BoxLayout.PAGE_AXIS));
		JScrollPane scrollEast = new JScrollPane(handEast, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		gamePane.add(scrollEast, "East");

		// setup discard pile
		discardPile = new JPanel();
		discardPile.setLayout(new GridBagLayout());
		gamePane.add(discardPile, "Center");
		// setup control buttons for game
		JPanel gameControl = new JPanel(new FlowLayout());
		// start game button
		JButton startButton = new JButton("Start Game");
		startButton.addActionListener(e -> sendStartGame());
		gameControl.add(startButton);
		// reset game button
		JButton resetButton = new JButton("Reset Game");
		resetButton.addActionListener(e -> sendResetGame());
		gameControl.add(resetButton);
		// draw card button
		JButton drawCardButton = new JButton("Draw Card");
		drawCardButton.addActionListener(e -> drawCard());
		gameControl.add(drawCardButton);
		gamePane.add(gameControl, "North");
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
			StringBuilder whisperRecipient = new StringBuilder(matcher.group(0));
			whisperRecipient.deleteCharAt(0);
			message.put("username", whisperRecipient);
			chatSend += " (Whispered from " + username + ")";
		}

		message.put("message", chatSend);
		protocol.sendMessage(message);
		printToChat(username + ": " + chatSend);
		chatInputArea.setText("");
	}

	public void printToChat(String chat) {
		chatArea.append(chat + "\n");
		chatArea.setCaretPosition(chatArea.getDocument().getLength());
	}

	private void sendStartGame() {
		resetGamePanel();
		printToChat("Requesting New Game\n");
		JSONObject message = new JSONObject();
		message.put("type", "application");
		JSONObject action = new JSONObject();
		action.put("action", "startGame");
		action.put("module", "juno");
		message.put("message", action);
		protocol.sendMessage(message);
	}

	private void sendResetGame() {
		JSONObject message = new JSONObject();
		message.put("type", "application");
		JSONObject action = new JSONObject();
		action.put("action", "reset");
		action.put("module", "juno");
		message.put("message", action);
		System.out.println("sent: " + message);
		protocol.sendMessage(message);
	}

	private void drawCard() {
		JSONObject message = new JSONObject();
		message.put("action", "dealCard");
		message.put("module", "juno");
		JSONObject dealCard = new JSONObject();
		dealCard.put("type", "application");
		dealCard.put("message", message);
		protocol.sendMessage(dealCard);
		System.out.println("recieved: " + dealCard);
	}

	private void placeCard(Card c, String playerHand) {
		handSouth.addCard(c);
		gamePane.updateUI();
	}

	public void resetGamePanel() {
		gamePane.removeAll();
		intializeGameArea();
		gamePane.updateUI();

	}

	public void handleDealCard(JSONObject m) {
		JSONObject message = new JSONObject(m.getString("card"));
		Card.Value value = Card.Value.valueOf(message.getString("value"));
		Card.Color color = Card.Color.valueOf(message.getString("color"));
		Card card = new Card(color, value);
		card.addActionListener(e -> playCard(value.toString(), color.toString()));
		placeCard(card, username);

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
		System.out.println("sent: " + message);
	}

	public void handleCardDealt(String player) {
		if (hands.containsKey(player)) {
			Hand hand = hands.get(player);
			hand.addCard(new Card(hand.getOrientaion()));
			gamePane.updateUI();
			return;
		} else if (!hands.containsValue(handWest)) {
			hands.put(player, handWest);
			handWest.addCard(new Card(handWest.getOrientaion()));
			gamePane.updateUI();
			return;
		} else if (!hands.containsValue(handEast)) {
			hands.put(player, handEast);
			handEast.addCard(new Card(handEast.getOrientaion()));
			gamePane.updateUI();
			return;
		} else if (!hands.containsValue(handNorth)) {
			hands.put(player, handNorth);
			handNorth.addCard(new Card(handNorth.getOrientaion()));
			gamePane.updateUI();
			return;
		}
		System.out.println("too many players");

	}

	public void updateDiscardPile(Card c) {
		discardPile.removeAll();
		discardPile.add(c);
		discardPile.updateUI();
	}

	public Hand getHandSouth() {
		return handSouth;
	}

	public Hand getHandNorth() {
		return handNorth;
	}

	public Hand getHandWest() {
		return handWest;
	}

	public Hand getHandEast() {
		return handEast;
	}
}
