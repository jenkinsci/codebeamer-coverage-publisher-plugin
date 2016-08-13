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
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class Line {

	@XmlAttribute(name = "nr")
	protected Integer nr;
	@XmlAttribute(name = "mi")
	protected Integer mi;
	@XmlAttribute(name = "ci")
	protected Integer ci;
	@XmlAttribute(name = "mb")
	protected Integer mb;
	@XmlAttribute(name = "cb")
	protected Integer cb;

	/**
	 * Gets the value of the nr property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getNr() {
		return this.nr;
	}

	/**
	 * Sets the value of the nr property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	public void setNr(Integer value) {
		this.nr = value;
	}

	/**
	 * Gets the value of the mi property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getMi() {
		return this.mi;
	}

	/**
	 * Sets the value of the mi property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	public void setMi(Integer value) {
		this.mi = value;
	}

	/**
	 * Gets the value of the ci property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getCi() {
		return this.ci;
	}

	/**
	 * Sets the value of the ci property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	public void setCi(Integer value) {
		this.ci = value;
	}

	/**
	 * Gets the value of the mb property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getMb() {
		return this.mb;
	}

	/**
	 * Sets the value of the mb property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	public void setMb(Integer value) {
		this.mb = value;
	}

	/**
	 * Gets the value of the cb property.
	 *
	 * @return possible object is {@link Integer }
	 *
	 */
	public Integer getCb() {
		return this.cb;
	}

	/**
	 * Sets the value of the cb property.
	 *
	 * @param value
	 *            allowed object is {@link Integer }
	 *
	 */
	public void setCb(Integer value) {
		this.cb = value;
	}

}