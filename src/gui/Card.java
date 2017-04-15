package gui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Card extends JLabel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -919021369126386690L;

	public static enum Color { RED, BLUE, GREEN, YELLOW };
	public static enum Value { ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, SKIP, DRAW2, REVERSE };
	private Color color;
	private Value value;	

	public Card(Color color, Value value) {
		setColor(color);
		setValue(value);
		setImage();
	}

	private void setImage() {
		ImageIcon cardImage = new ImageIcon(getClass().getResource("/resources/red-00.png"));
		setIcon(cardImage);
		
	}

	private Color getColor() {
		return color;
	}

	private void setColor(Color color) {
		this.color = color;
	}

	private Value getValue() {
		return value;
	}

	private void setValue(Value value) {
		this.value = value;
	}
	public static void main(String[] args) {
		
	}

}
