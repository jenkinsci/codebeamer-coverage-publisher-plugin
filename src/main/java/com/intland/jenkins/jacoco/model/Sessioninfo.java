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
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="start" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="dump" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class Sessioninfo {

	@XmlAttribute(name = "id")
	protected String id;
	@XmlAttribute(name = "start")
	protected Integer start;
	@XmlAttribute(name = "dump")
	protected Integer dump;

	/**
	 * Gets the value of the id property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Sets the value of the id property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setId(String value) {
		this.id = value;
	}

	/**
	 * Gets the value of the start property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getStart() {
		return this.start;
	}

	/**
	 * Sets the value of the start property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	public void setStart(Integer value) {
		this.start = value;
	}

	/**
	 * Gets the value of the dump property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getDump() {
		return this.dump;
	}

	/**
	 * Sets the value of the dump property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	public void setDump(Integer value) {
		this.dump = value;
	}

}