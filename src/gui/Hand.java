package gui;

import java.util.ArrayList;

import javax.swing.JPanel;

public class Hand extends JPanel {
	private String username;
	private ArrayList<Card> cards;
	public Hand(String username) {
		this.username = username;
		cards = new ArrayList<>();
		
	}
	public void addCard(Card c) {
		this.add(c);
		cards.add(c);
	}
	public String getUserName() {
		return username;
	}
	
	

}
