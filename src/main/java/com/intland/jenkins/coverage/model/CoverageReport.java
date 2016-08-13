package com.intland.jenkins.coverage.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Coverage result for a full source code
 *
 * @author abanfi
 *
 */
public class CoverageReport extends CoverageBase {

	public static enum CoverageType {
		JACOCO, COBERTURA
	}

	/**
	 * Coverage informations for packages, directories or namespaces
	 */
	private List<DirectoryCoverage> directories = new ArrayList<>();

	private CoverageType type;

	public List<DirectoryCoverage> getDirectories() {
		return this.directories;
	}

	public void setDirectories(List<DirectoryCoverage> directories) {
		this.directories = directories;
	}

	public String getStatus() {
		// TODO
		return "SUCCESS";
	}

	public String toSummary() {
		int directoryCount = 0;
		int classesCount = 0;

		for (DirectoryCoverage directoryCoverage : this.directories) {
			directoryCount++;
			classesCount += directoryCoverage.getFiles().size();
		}

		return String.format("Report <%s> contains %d directories and %d classes!", this.getName(), directoryCount,
				classesCount);
	}

	public CoverageType getType() {
		return this.type;
	}

	public void setType(CoverageType type) {
		this.type = type;
	}

}
