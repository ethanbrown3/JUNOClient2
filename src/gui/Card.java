package gui;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Card extends JButton {
	private static final long serialVersionUID = 1700177456984817126L;

	static enum Color {
		RED, BLUE, GREEN, YELLOW, WILD
	};

	static enum Value {
		ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, SKIP, DRAW2, REVERSE, WILD, WILDD4
	};

	static enum CardOrientation {
		LEFT, RIGHT, UP
	};

	private Color color;
	private Value value;
	private CardOrientation orientation;

	Card(Color color, Value value) {
		setColor(color);
		setValue(value);
		setImage();
	}

	Card(CardOrientation o) {
		setOrientation(o);
		setBackImage();
	}

	private void setBackImage() {
		ImageIcon cardImage = new ImageIcon(
				getClass().getResource("/images/back" + "-" + getOrientation().toString().toLowerCase() + ".png"));
		setIcon(cardImage);

	}

	private void setImage() {
		ImageIcon cardImage = new ImageIcon(
				getClass().getResource("/images/" + getColor().toString() + "-" + getValue().toString() + ".png"));
		setIcon(cardImage);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Value getValue() {
		return value;
	}

	private void setValue(Value value) {
		this.value = value;
	}

	private CardOrientation getOrientation() {
		return orientation;
	}

	private void setOrientation(CardOrientation orientation) {
		this.orientation = orientation;
	}

	public boolean equals(Card card) {

		return ((this.getValue().equals(card.getValue())) && (this.getColor().equals(card.getColor())));
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
