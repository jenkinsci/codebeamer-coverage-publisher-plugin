package com.intland.jenkins.coverage.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Coverage informations for a directory, namespace or package
 *
 * @author abanfi
 */
public class DirectoryCoverage extends CoverageBase {

	/**
	 * Source files coverage
	 */
	private List<CoverageBase> files = new ArrayList<>();

	public List<CoverageBase> getFiles() {
		return this.files;
	}

	public void setFiles(List<CoverageBase> files) {
		this.files = files;
	}

	@Override
	public String getName() {
		String name = super.getName();
		return StringUtils.isBlank(name) ? "default" : name;
	}

}
