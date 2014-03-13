package de.andreas.harter.yaaarc.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RollingGrid extends HBox {

	private static final String FXML_PATH = "/fxml/components/RollingGrid.fxml";
	private static final int FROM_COLUMN_INDEX = 0;
	private static final int TO_COLUMN_INDEX = 2;

	@FXML
	private ScrollBar scrollBar;
	@FXML
	private VBox scrollContainer;

	private SelectedMainRowListener selectedMainRowListener;

	private List<String> from = new ArrayList<String>();
	private List<String> to = new ArrayList<String>();

	public interface SelectedMainRowListener {

		public void selected(String selectedFromID);

	}

	public RollingGrid() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML_PATH));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public void setup() {
		scrollBar.valueProperty().addListener(new ChangeListener<Number>() {

			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				selectEntry();
			}
		});
	}

	public void setFrom(List<String> from) {
		this.from = from;
		reset();
	}

	public void setTo(List<String> to) {
		this.to = to;
		reset();
		selectEntry();
	}

	public void clear() {
		reset();
	}

	public void registerSelectedMainRowListener(SelectedMainRowListener selectedMainRowListener) {
		this.selectedMainRowListener = selectedMainRowListener;
	}

	@FXML
	private void containerScroll(ScrollEvent event) {
		double movement = event.getDeltaY() / Math.abs(event.getDeltaY());
		// reverse direction
		movement *= -1;
		if (scrollBar.getMin() == scrollBar.getValue() && movement < 0) {
			return;
		}
		if (scrollBar.getMax() == scrollBar.getValue() && movement > 0) {
			return;
		}
		scrollBar.setValue(scrollBar.getValue() + movement);
	}

	private int calculateMainRowIndex() {
		int containerSize = scrollContainer.getChildren().size();
		int mainRowIndex = Math.round(containerSize / 2);
		return mainRowIndex;
	}

	private void reset() {
		resetScrollbar();
		resetGrid();
	}

	private void resetGrid() {
		for (int i = 0; i < scrollContainer.getChildren().size(); i++) {
			setFromRowEntry(i, "");
			setToRowEntry(i, "");
		}
	}

	private void resetScrollbar() {
		scrollBar.setValue(scrollBar.getMin());
		int maxScrollValue = from.size() < to.size() ? from.size() : to.size();
		scrollBar.setMax(maxScrollValue - 1);
	}

	private void selectEntry() {
		resetGrid();
		int scrollValue = (int) scrollBar.getValue();
		int mainRowIndex = calculateMainRowIndex();
		for (int i = 0; i <= mainRowIndex; i++) {
			int lowerTableIndex = mainRowIndex - i;
			int lowerSelectionIndex = scrollValue - i;
			setEntry(FROM_COLUMN_INDEX, lowerTableIndex, from, lowerSelectionIndex);
			setEntry(TO_COLUMN_INDEX, lowerTableIndex, to, lowerSelectionIndex);
			int upperTableIndex = mainRowIndex + i;
			int upperSelectionIndex = scrollValue + i;
			setEntry(FROM_COLUMN_INDEX, upperTableIndex, from, upperSelectionIndex);
			setEntry(TO_COLUMN_INDEX, upperTableIndex, to, upperSelectionIndex);
		}
		informListener();
	}

	private void setEntry(int columnIndex, int rowIndex, List<String> text, int textIndex) {
		String labelText = "";
		if (text != null && textIndex >= 0 && text.size() > textIndex) {
			labelText = text.get(textIndex);
		}
		setRowEntry(columnIndex, rowIndex, labelText);
	}

	private void setFromRowEntry(int rowIndex, String text) {
		setRowEntry(FROM_COLUMN_INDEX, rowIndex, text);
	}

	private void setToRowEntry(int rowIndex, String text) {
		setRowEntry(TO_COLUMN_INDEX, rowIndex, text);
	}

	private void setRowEntry(int columnIndex, int rowIndex, String text) {
		HBox rowContainer = (HBox) scrollContainer.getChildren().get(rowIndex);
		Label label = (Label) rowContainer.getChildren().get(columnIndex);
		label.setText(text);
	}

	private void informListener() {
		if (selectedMainRowListener == null) {
			return;
		}

		int mainRowIndex = calculateMainRowIndex();
		HBox rowContainer = (HBox) scrollContainer.getChildren().get(mainRowIndex);
		Label label = (Label) rowContainer.getChildren().get(0);
		selectedMainRowListener.selected(label.getText());
	}

}
