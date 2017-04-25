package gui;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import gui.Card.CardOrientation;

public class Hand extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5586729830433228986L;
	private String username;
	private ArrayList<Card> cards;
	private CardOrientation orientation;

	public Hand(CardOrientation o) {
		setOrientation(o);
		cards = new ArrayList<>();
	}

	public void addCard(Card c) {
		add(c);
		cards.add(c);
		updateUI();
	}

	public void setUserName(String user) {
		this.username = user;
		this.setBorder(new TitledBorder(username));
	}

	public void removeCard(Card card) {
		System.out.println("removeCard()");
		int index = 0;
		for (Card c : cards) {
			if (card.equals(c)) {
				this.remove(cards.remove(index));
				this.updateUI();
				break;
			}
			index++;
		}
	}
	
	public void setTurnHighlight() {
		this.setBackground(Color.LIGHT_GRAY);
	}
	public void resetTurnHighlight() {
		this.setBackground(null);
	}

	public void removeBlankCard() {
		if (!cards.isEmpty()) {
			this.remove(cards.get(0));
			cards.remove(0);
		}
	}

	public CardOrientation getOrientaion() {
		return orientation;
	}

	private void setOrientation(CardOrientation o) {
		orientation = o;
	}

}
