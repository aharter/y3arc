package de.andreas.harter.yaaarc.components;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextArea;

public class BlockTextArea extends TextArea {

	private IntegerProperty characterBlockSize = new SimpleIntegerProperty(1);

	public IntegerProperty characterBlockSize() {
		return characterBlockSize;
	}

	@Override
	public void replaceText(int start, int end, String text) {
		super.replaceText(start, end, text);
		formatTextToBlock();
	}

	@Override
	public void replaceSelection(String replacement) {
		super.replaceSelection(replacement);
		formatTextToBlock();
	}

	public int getCharacterBlockSize() {
		return characterBlockSize.get();
	}

	public void setCharacterBlockSize(int characterBlockSize) {
		this.characterBlockSize.set(characterBlockSize);
	}

	public List<String> getBlocks() {
		return getBlocks(true);
	}

	private List<String> getBlocks(boolean onlyFullBlocks) {
		String text = getText().replace(" ", "");
		int upperBorder = text.length() - (text.length() % getCharacterBlockSize());
		List<String> blockList = new ArrayList<String>();
		for (int i = 0; i < upperBorder; i += getCharacterBlockSize()) {
			blockList.add(text.substring(i, i + getCharacterBlockSize()));
		}

		if (!onlyFullBlocks) {
			blockList.add(text.substring(upperBorder, text.length()));
		}
		return blockList;
	}

	private void formatTextToBlock() {
		StringBuilder builder = new StringBuilder();
		for (String block : getBlocks(false)) {
			builder.append(block.toUpperCase());
			builder.append(" ");
		}
		int index = builder.length() - 1;
		while (index > 0 && index < builder.length() && builder.charAt(index) == ' ') {
			builder.deleteCharAt(index);
			index--;
		}
		this.textProperty().set(builder.toString());

		int carretIndex = getText().length();
		positionCaret(carretIndex);
	}

}
