package gui;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Card extends JButton {
	private static final long serialVersionUID = 1700177456984817126L;

	/**
	 * 
	 */
	static enum Color {
		RED, BLUE, GREEN, YELLOW, WILD
	};

	static enum Value {
		ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, SKIP, DRAW2, REVERSE, WILD, WILDD4
	};

	private Color color;
	private Value value;

	Card(Color color, Value value) {
		setColor(color);
		setValue(value);
		setImage();
	}

	private void setImage() {
		ImageIcon cardImage = new ImageIcon(
				getClass().getResource("/images/" + getColor().toString() + "-" + getValue().toString() + ".png"));
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

}
