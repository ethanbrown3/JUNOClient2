package gui;

import java.util.ArrayList;

import javax.swing.JPanel;

public class Hand extends JPanel {
	private String username;
	private ArrayList<Card> cards;

	public Hand() {
		// this.username = username;
		cards = new ArrayList<>();

	}

	public void addCard(Card c) {
		this.add(c);
		cards.add(c);
	}

	public String getUserName() {
		return username;
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

}
