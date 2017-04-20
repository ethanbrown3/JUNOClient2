package gui;

import java.util.ArrayList;

import javax.swing.JPanel;

public class Hand extends JPanel {
	private String username;
	private ArrayList<Card> cards;
	public Hand() {
//		this.username = username;
		cards = new ArrayList<>();
		
	}
	public void addCard(Card c) {
		this.add(c);
		cards.add(c);
	}
	public String getUserName() {
		return username;
	}
	
	public void removeCard(Card c) {
		System.out.println("removeCard()");
		int index = 0;
		for (Card i : cards) {
			if (c.equals(i))
				break;
			index++;
		}
		
		this.remove(cards.remove(index));
		this.updateUI();
	}
	
	

}
