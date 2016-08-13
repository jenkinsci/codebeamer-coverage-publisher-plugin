package com.intland.jenkins.gcovr;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.intland.jenkins.coverage.model.CoverageReport;

public class GcovrResultParserTest {

	@Test
	public void testCollectCoverageReport() throws IOException {
		URL resource = this.getClass().getResource("/coverage.xml");

		GcovResultParser parser = new GcovResultParser();
		CoverageReport collectCoverageReport = parser.collectCoverageReport(resource.getPath(), null);

		Assert.assertEquals(192, collectCoverageReport.getLineCovered().intValue());
		Assert.assertEquals(5, collectCoverageReport.getLineMissed().intValue());

		Assert.assertEquals(233, collectCoverageReport.getBranchCovered().intValue());
		Assert.assertEquals(970 - 233, collectCoverageReport.getBranchMissed().intValue());

		Assert.assertEquals(11, collectCoverageReport.getClassCovered().intValue());
		Assert.assertEquals(0, collectCoverageReport.getClassMissed().intValue());

	}
}
