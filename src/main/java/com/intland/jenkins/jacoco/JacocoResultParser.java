package com.intland.jenkins.jacoco;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.intland.jenkins.coverage.ExecutionContext;
import com.intland.jenkins.coverage.ICoverageCoverter;
import com.intland.jenkins.coverage.markup.HTMLMarkupBuilder;
import com.intland.jenkins.coverage.model.CoverageBase;
import com.intland.jenkins.coverage.model.CoverageReport;
import com.intland.jenkins.coverage.model.CoverageReport.CoverageType;
import com.intland.jenkins.coverage.model.DirectoryCoverage;
import com.intland.jenkins.jacoco.model.Class;
import com.intland.jenkins.jacoco.model.Counter;
import com.intland.jenkins.jacoco.model.Group;
import com.intland.jenkins.jacoco.model.Package;
import com.intland.jenkins.jacoco.model.Report;

/**
 * Coverage parser implementation for jacoco reports
 *
 * @author abanfi
 */
public class JacocoResultParser implements ICoverageCoverter {

	@Override
	public CoverageReport collectCoverageReport(String reportFilePath, ExecutionContext context) throws IOException {
		try {

			// create parser
			JAXBContext jaxbContext = JAXBContext.newInstance(Report.class);
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			spf.setFeature("http://xml.org/sax/features/validation", false);
			XMLReader xmlReader = spf.newSAXParser().getXMLReader();
			InputSource inputSource = new InputSource(new FileReader(reportFilePath));
			SAXSource source = new SAXSource(xmlReader, inputSource);

			// unmarshall the XML
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Report report = (Report) jaxbUnmarshaller.unmarshal(source);

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
	private CoverageReport convertToCoverageReport(Report report) {

		CoverageReport coverageReport = new CoverageReport();
		coverageReport.setName(report.getName());

		// groups - greater compilation unit, eg. a project
		List<Group> groups = report.getGroup();
		if (groups != null) {
			for (Group group : groups) {
				for (Package pack : group.getPackage()) {
					coverageReport.getDirectories().add(this.converPackage(pack));
				}
			}
		}

		// simple packages
		List<Package> packages = report.getPackage();
		// sort packages by name
		Collections.sort(packages, new Comparator<Package>() {

			@Override
			public int compare(Package p1, Package p2) {
				return ObjectUtils.compare(p1.getName(), p2.getName());
			}

		});

		for (Package onePackage : packages) {
			coverageReport.getDirectories().add(this.converPackage(onePackage));
		}

		this.setCoverage(coverageReport, report.getCounter());
		coverageReport.setMarkup(HTMLMarkupBuilder.genearteSummary(report));

		coverageReport.setType(CoverageType.JACOCO);

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

		for (Class clazz : onePackage.getClazz()) {
			directoryCoverage.getFiles().add(this.converClass(clazz, onePackage.getName()));
		}

		this.setCoverage(directoryCoverage, onePackage.getCounter());
		directoryCoverage.setMarkup(HTMLMarkupBuilder.genearteSummary(onePackage));

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
	private CoverageBase converClass(Class clazz, String packageName) {

		CoverageBase base = new CoverageBase();
		base.setName(StringUtils.replace(clazz.getName(), packageName + "/", ""));

		this.setCoverage(base, clazz.getCounter());
		base.setMarkup(HTMLMarkupBuilder.genearteSummary(clazz));

		return base;
	}

	private void setCoverage(CoverageBase base, List<Counter> counters) {
		for (Counter counter : counters) {
			String type = StringUtils.lowerCase(counter.getType());
			switch (type) {
			case "line":
				base.setLineCovered(counter.getCovered());
				base.setLineMissed(counter.getMissed());
				break;
			case "instruction":
				base.setInstructionCovered(counter.getCovered());
				base.setInstructionMissed(counter.getMissed());
				break;
			case "complexity":
				base.setComplexityCovered(counter.getCovered());
				base.setComplexityMissed(counter.getMissed());
				break;
			case "method":
				base.setMethodCovered(counter.getCovered());
				base.setMethodMissed(counter.getMissed());
				break;
			case "branch":
				base.setBranchCovered(counter.getCovered());
				base.setBranchMissed(counter.getMissed());
				break;
			case "class":
				base.setClassCovered(counter.getCovered());
				base.setClassMissed(counter.getMissed());
				break;
			default:
				break;
			}
		}
	}

}
