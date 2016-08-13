package com.intland.jenkins.gcovr;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.intland.jenkins.coverage.ExecutionContext;
import com.intland.jenkins.coverage.ICoverageCoverter;
import com.intland.jenkins.coverage.model.CoverageBase;
import com.intland.jenkins.coverage.model.CoverageReport;
import com.intland.jenkins.coverage.model.CoverageReport.CoverageType;
import com.intland.jenkins.coverage.model.DirectoryCoverage;
import com.intland.jenkins.gcovr.model.ClassesType;
import com.intland.jenkins.gcovr.model.Coverage;
import com.intland.jenkins.gcovr.model.Line;
import com.intland.jenkins.gcovr.model.Lines;
import com.intland.jenkins.gcovr.model.Package;
import com.intland.jenkins.gcovr.model.Packages;

/**
 * Coverage parser implementation for jacoco reports
 *
 * @author abanfi
 */
public class GcovResultParser implements ICoverageCoverter {

	@Override
	public CoverageReport collectCoverageReport(String reportFilePath, ExecutionContext context) throws IOException {
		try {

			// create parser
			JAXBContext jaxbContext = JAXBContext.newInstance(Coverage.class);
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			spf.setFeature("http://xml.org/sax/features/validation", false);
			XMLReader xmlReader = spf.newSAXParser().getXMLReader();
			InputSource inputSource = new InputSource(new FileReader(reportFilePath));
			SAXSource source = new SAXSource(xmlReader, inputSource);

			// unmarshall the XML
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Coverage report = (Coverage) jaxbUnmarshaller.unmarshal(source);

			// convert result to the common form
			return this.convertToCoverageReport(report);

		} catch (Exception e) {
			context.logFormat("Exception occurred during parse the jacoco result: %s", e.getMessage());
			throw new IOException(e);
		}
	}

	/**
	 * Converts a report and it's children to a coverage report object
	 *
	 * @param report
	 *            the report to convert
	 * @return the coverage report
	 */
	private CoverageReport convertToCoverageReport(Coverage report) {

		CoverageReport coverageReport = new CoverageReport();
		coverageReport.setName("Gobertura coverage");

		// simple packages
		Packages packages = report.getPackages();
		for (Package onePackage : packages.getPackage()) {
			coverageReport.getDirectories().add(this.converPackage(onePackage));
		}

		int missed = 0;
		int covered = 0;
		int missedConditional = 0;
		int coveredConditional = 0;
		int classesCovered = 0;
		int classesMissed = 0;

		for (DirectoryCoverage classCoverage : coverageReport.getDirectories()) {
			missed += classCoverage.getLineMissed();
			covered += classCoverage.getLineCovered();
			missedConditional += classCoverage.getBranchMissed();
			coveredConditional += classCoverage.getBranchCovered();
			classesCovered += classCoverage.getClassCovered();
			classesMissed += classCoverage.getClassMissed();
		}

		coverageReport.setLineCovered(covered);
		coverageReport.setLineMissed(missed);

		coverageReport.setBranchCovered(coveredConditional);
		coverageReport.setBranchMissed(missedConditional);

		coverageReport.setClassCovered(classesCovered);
		coverageReport.setClassMissed(classesMissed);

		coverageReport.setMarkup(GcovHTMLMarkupBuilder.genearteSummary(coverageReport));

		coverageReport.setType(CoverageType.COBERTURA);

		return coverageReport;
	}

	/**
	 * Converts a package object and it's children to a directory base object
	 *
	 * @param onePackage
	 *            the package to convert
	 * @return the directory base object
	 */
	private DirectoryCoverage converPackage(Package onePackage) {

		DirectoryCoverage directoryCoverage = new DirectoryCoverage();
		directoryCoverage.setName(StringUtils.replace(onePackage.getName(), "/", "."));

		ClassesType classes = onePackage.getClasses();
		if (classes != null) {
			for (com.intland.jenkins.gcovr.model.Class clazz : classes.getClazz()) {
				directoryCoverage.getFiles().add(this.convertClass(clazz, onePackage.getName()));
			}
		}

		int missed = 0;
		int covered = 0;
		int missedConditional = 0;
		int coveredConditional = 0;
		int classesCovered = 0;
		int classesMissed = 0;

		for (CoverageBase classCoverage : directoryCoverage.getFiles()) {
			missed += classCoverage.getLineMissed();
			covered += classCoverage.getLineCovered();
			missedConditional += classCoverage.getBranchMissed();
			coveredConditional += classCoverage.getBranchCovered();

			if (classCoverage.getLineCovered() > 0) {
				classesCovered++;
			} else {
				classesMissed++;
			}
		}

		directoryCoverage.setLineCovered(covered);
		directoryCoverage.setLineMissed(missed);

		directoryCoverage.setBranchCovered(coveredConditional);
		directoryCoverage.setBranchMissed(missedConditional);

		directoryCoverage.setClassCovered(classesCovered);
		directoryCoverage.setClassMissed(classesMissed);

		directoryCoverage.setMarkup(GcovHTMLMarkupBuilder.genearteSummary(directoryCoverage));

		return directoryCoverage;
	}

	/**
	 * Converts a class object to a coverage base object
	 *
	 * @param clazz
	 *            the class to convert
	 * @param packageName
	 *            the parent package's name
	 * @return a coverage base object
	 */
	private CoverageBase convertClass(com.intland.jenkins.gcovr.model.Class clazz, String packageName) {

		CoverageBase base = new CoverageBase();
		base.setName(StringUtils.replace(clazz.getName(), packageName + "/", ""));

		this.setCoverage(base, clazz);
		base.setMarkup(GcovHTMLMarkupBuilder.genearteSummary(base));

		return base;
	}

	private void setCoverage(CoverageBase base, com.intland.jenkins.gcovr.model.Class clazz) {

		Lines allLines = clazz.getLines();
		if (allLines != null) {
			List<Line> lines = allLines.getLine();

			int missed = 0;
			int covered = 0;
			int allConditional = 0;
			int coveredConditional = 0;

			for (Line line : lines) {
				if ("0".equals(line.getHits())) {
					missed++;
				} else {
					covered++;
				}

				// example value: condition-coverage="100% (2/2)"
				if ("true".equals(line.getBranch())) {
					String conditionCoverage = line.getConditionCoverage();
					String[] split = StringUtils.split(conditionCoverage);
					if ((split != null) && (split.length == 2)) {
						String coveragePart = split[1];
						if (coveragePart != null) {
							coveragePart = coveragePart.replace("(", "").replace(")", "");
							String[] coverageResult = StringUtils.split(coveragePart, "/");

							if ((coverageResult != null) && (coverageResult.length == 2)) {
								coveredConditional += Integer.valueOf(coverageResult[0]);
								allConditional += Integer.valueOf(coverageResult[1]);
							}
						}
					}
				}
			}

			base.setLineCovered(covered);
			base.setLineMissed(missed);

			base.setBranchCovered(coveredConditional);
			base.setBranchMissed(allConditional - coveredConditional);
		}
	}
}
