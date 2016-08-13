package com.intland.jenkins.jacoco.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "clazz", "sourcefile", "counter" })
public class Package {

	@XmlElement(name = "class", required = true)
	protected List<Class> clazz;
	@XmlElement(required = true)
	protected List<Sourcefile> sourcefile;
	@XmlElement(required = true)
	protected List<Counter> counter;
	@XmlAttribute(name = "name")
	protected String name;

	/**
	 * Gets the value of the clazz property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the clazz property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getClazz().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Report.Package.Class }
	 *
	 *
	 */
	public List<Class> getClazz() {
		if (this.clazz == null) {
			this.clazz = new ArrayList<Class>();
		}
		return this.clazz;
	}

	/**
	 * Gets the value of the sourcefile property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the sourcefile property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getSourcefile().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Report.Package.Sourcefile }
	 *
	 *
	 */
	public List<Sourcefile> getSourcefile() {
		if (this.sourcefile == null) {
			this.sourcefile = new ArrayList<Sourcefile>();
		}
		return this.sourcefile;
	}

	/**
	 * Gets the value of the counter property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the counter property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getCounter().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Report.Package.Counter }
	 *
	 *
	 */
	public List<Counter> getCounter() {
		if (this.counter == null) {
			this.counter = new ArrayList<Counter>();
		}
		return this.counter;
	}

	/**
	 * Gets the value of the name property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the value of the name property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setName(String value) {
		this.name = value;
	}

}