package de.andreas.harter.yaaarc;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import de.andreas.harter.yaaarc.components.BlockTextArea;
import de.andreas.harter.yaaarc.components.RollingGrid;
import de.andreas.harter.yaaarc.components.RollingGrid.SelectedMainRowListener;
import de.andreas.harter.yaaarc.db.DbAccess;
import de.andreas.harter.yaaarc.model.AminoAcid;
import de.andreas.harter.yaaarc.model.BasePair;
import de.andreas.harter.yaaarc.model.CytosolBasePair;
import de.andreas.harter.yaaarc.model.MitochondriaBasePair;
import de.andreas.harter.yaaarc.model.TransformMode;

public class YaaarcController implements Initializable {

	private static final String INVALID_INPUT_MESSAGE = "Invalid Input!";
	private static final int OUTPUT_BLOCK_SIZE = 3;

	@FXML
	private ToggleGroup toggle;
	@FXML
	private RollingGrid rollingGrid;
	@FXML
	private BlockTextArea input;
	@FXML
	private BlockTextArea output;
	@FXML
	private VBox probabilityTable;

	private BooleanProperty invalid = new SimpleBooleanProperty(false);

	public void initialize(URL location, ResourceBundle resources) {
		output.setCharacterBlockSize(OUTPUT_BLOCK_SIZE);
		input.setOnKeyReleased(new EventHandler<KeyEvent>() {

			public void handle(KeyEvent event) {
				textChanged();
			}
		});
		toggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				input.setCharacterBlockSize(getCodingSize());
				input.setText("");
				textChanged();
			}
		});
		rollingGrid.setup();
		rollingGrid.registerSelectedMainRowListener(new SelectedMainRowListener() {

			public void selected(String selectedFromID) {
				setProbabilityTable(selectedFromID);
			}
		});
		output.disableProperty().bind(invalid);
		rollingGrid.disableProperty().bind(invalid);
	}

	private void textChanged() {
		output.setText("");
		invalid.set(false);
		for (String id : input.getBlocks()) {
			processID(id.toUpperCase());
		}
		if (!invalid.get()) {
			rollingGrid.setFrom(input.getBlocks());
			rollingGrid.setTo(output.getBlocks());
		}
	}

	private int getCodingSize() {
		TransformMode mode = (TransformMode) toggle.getSelectedToggle().getUserData();
		return mode == TransformMode.AMINO_ACID_TO_CYTOSOL || mode == TransformMode.AMINO_ACID_TO_MITOCHONDRIA ? 1 : 3;
	}

	private void processID(String id) {
		String coding = getCoding(id);
		if (coding != null && !invalid.get()) {
			appendOutput(coding);
		} else {
			output.setText(INVALID_INPUT_MESSAGE);
			rollingGrid.clear();
			invalid.set(true);
		}
	}

	private String getCoding(String id) {
		String coding = null;
		TransformMode mode = (TransformMode) toggle.getSelectedToggle().getUserData();
		switch (mode) {
		case AMINO_ACID_TO_MITOCHONDRIA:
			coding = recodeAminoAcidToMitochondria(id);
			break;
		case AMINO_ACID_TO_CYTOSOL:
			coding = recodeAminoAcidToCytosol(id);
			break;
		case CYTOSOL_TO_MITOCHONDRIA:
			coding = recodeCytosolToMitochondria(id);
			break;
		default:
			coding = null;
		}
		return coding;
	}

	private void appendOutput(String appendString) {
		setOutput(output.getText() + appendString);
	}

	private void setOutput(String setOutput) {
		output.setText(setOutput);
	}

	private String recodeAminoAcidToMitochondria(String aminoAcidID) {
		List<MitochondriaBasePair> basePair = queryMitochondriaBasePairsForAminoAcid(aminoAcidID);
		String mitochondiraCoding = null;
		setBasePairProbability(basePair);
		if (basePair.size() > 0) {
			mitochondiraCoding = basePair.get(0).getCoding();
		}
		return mitochondiraCoding;
	}

	private String recodeAminoAcidToCytosol(String aminoAcidID) {
		List<CytosolBasePair> basePair = queryCytosolBasePairForAminoAcid(aminoAcidID);
		String cytosolCoding = null;
		setBasePairProbability(basePair);
		if (basePair.size() > 0) {
			cytosolCoding = basePair.get(0).getCoding();
		}
		return cytosolCoding;
	}

	private String recodeCytosolToMitochondria(String cytosolBasePairCoding) {
		List<MitochondriaBasePair> basePair = queryMitochondriaBasePairsForCytosolBasePair(cytosolBasePairCoding);
		setBasePairProbability(basePair);
		String mitochondiraCoding = null;
		if (basePair.size() > 0) {
			mitochondiraCoding = basePair.get(0).getCoding();
		}
		return mitochondiraCoding;
	}

	private List<MitochondriaBasePair> queryMitochondriaBasePairsForAminoAcid(String aminoAcidID) {
		AminoAcid aminoAcid = new AminoAcid();
		aminoAcid.setIdentifier(aminoAcidID);
		List<MitochondriaBasePair> basePair = DbAccess.getInstance().queryMitochondriaBasePairsForAminoAcid(aminoAcid);
		return basePair;
	}

	private List<CytosolBasePair> queryCytosolBasePairForAminoAcid(String aminoAcidID) {
		AminoAcid aminoAcid = new AminoAcid();
		aminoAcid.setIdentifier(aminoAcidID);
		List<CytosolBasePair> basePair = DbAccess.getInstance().queryCytosolBasePairForAminoAcid(aminoAcid);
		return basePair;
	}

	private List<MitochondriaBasePair> queryMitochondriaBasePairsForCytosolBasePair(String cytosolBasePairCoding) {
		CytosolBasePair cytosolBasePair = new CytosolBasePair();
		cytosolBasePair.setCoding(cytosolBasePairCoding);
		List<MitochondriaBasePair> basePair = DbAccess.getInstance().queryMitochondriaBasePairsForCytosolBasePair(
				cytosolBasePair);

		return basePair;

	}

	private void setProbabilityTable(String selectedID) {
		TransformMode mode = (TransformMode) toggle.getSelectedToggle().getUserData();
		switch (mode) {
		case CYTOSOL_TO_MITOCHONDRIA:
			setBasePairProbability(queryMitochondriaBasePairsForCytosolBasePair(selectedID));
			break;
		case AMINO_ACID_TO_MITOCHONDRIA:
			setBasePairProbability(queryMitochondriaBasePairsForAminoAcid(selectedID));
			break;
		case AMINO_ACID_TO_CYTOSOL:
			setBasePairProbability(queryCytosolBasePairForAminoAcid(selectedID));
			break;
		}
	}

	private void setBasePairProbability(List<? extends BasePair> basePairs) {
		probabilityTable.getChildren().clear();
		for (BasePair basePair : basePairs) {
			addProbabilityLabel(basePair.getCoding(), basePair.getProbability());
		}
	}

	private void addProbabilityLabel(String coding, int probability) {
		StringBuilder builder = new StringBuilder(coding);
		builder.append(":  ");
		builder.append(probability);
		builder.append(" %");
		Label label = new Label(builder.toString());
		probabilityTable.getChildren().add(label);
	}

}
