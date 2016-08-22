package com.intland.jenkins.jacoco;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.intland.jenkins.coverage.model.CoverageReport;

public class JacocoResultParserTest {

	@Test
	public void testCollectCoverageReport() throws IOException {
		URL resource = this.getClass().getResource("/jacoco.xml");

		JacocoResultParser parser = new JacocoResultParser();
		CoverageReport collectCoverageReport = parser.collectCoverageReport(resource.getPath(), null);

		Integer lineCoverage = collectCoverageReport.getLineCovered();
		Assert.assertEquals(Double.valueOf(18d), Double.valueOf(lineCoverage.toString()));
	}
}
