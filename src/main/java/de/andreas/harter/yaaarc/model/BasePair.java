package de.andreas.harter.yaaarc.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.OneToOne;

public abstract class BasePair {

	@Id
	private String coding;
	@Column(nullable = false)
	private int probability;
	@OneToOne(optional = false)
	private AminoAcid aminoAcid;

	public AminoAcid getAminoAcid() {
		return aminoAcid;
	}

	public void setAminoAcid(AminoAcid aminoAcid) {
		this.aminoAcid = aminoAcid;
	}

	public String getCoding() {
		return coding;
	}

	public void setCoding(String coding) {
		this.coding = coding;
	}

	public int getProbability() {
		return probability;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

}
