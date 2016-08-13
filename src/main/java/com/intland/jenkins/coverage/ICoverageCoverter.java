package com.intland.jenkins.coverage;

import java.io.IOException;

import com.intland.jenkins.coverage.model.CoverageReport;

/**
 * Interface for coverage coverters
 *
 * @author abanfi
 */
public interface ICoverageCoverter {

	/**
	 * Collect coverage report in the specified file
	 *
	 * @param reportFilePath
	 *            path to the report result file
	 * @param context
	 *            the execution context
	 * @return the parsed coverage report object
	 * @throws IOException
	 */
	public CoverageReport collectCoverageReport(String reportFilePath, ExecutionContext context) throws IOException;

}
