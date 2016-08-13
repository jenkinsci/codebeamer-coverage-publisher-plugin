package com.intland.jenkins.jacoco.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="missed" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="covered" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class Counter {

	@XmlAttribute(name = "type")
	protected String type;
	@XmlAttribute(name = "missed")
	protected Integer missed;
	@XmlAttribute(name = "covered")
	protected Integer covered;

	/**
	 * Gets the value of the type property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Sets the value of the type property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setType(String value) {
		this.type = value;
	}

	/**
	 * Gets the value of the missed property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getMissed() {
		return this.missed;
	}

	/**
	 * Sets the value of the missed property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	public void setMissed(Integer value) {
		this.missed = value;
	}

	/**
	 * Gets the value of the covered property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getCovered() {
		return this.covered;
	}

	/**
	 * Sets the value of the covered property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	public void setCovered(Integer value) {
		this.covered = value;
	}

}