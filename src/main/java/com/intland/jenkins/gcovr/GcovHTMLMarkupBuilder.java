package com.intland.jenkins.gcovr;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.intland.jenkins.coverage.model.CoverageBase;
import com.intland.jenkins.coverage.model.CoverageReport;
import com.intland.jenkins.coverage.model.DirectoryCoverage;

/**
 * HTML markup builder for a jacoco coverage result
 *
 * @author abanfi
 *
 */
public class GcovHTMLMarkupBuilder {

	private static String createHeader(boolean needPackages, boolean needClasses) {
		StringBuilder builder = new StringBuilder(
				"<table class=\"trackerItems relationsExpander displaytag treetable trackerItemTreeTable\" style=\"width: 300px;\">"
						+ "<thead><tr><th style=\"min-width: 200px;\" class=\"textData\">Name</th>");

		if (needPackages) {
			builder.append("<th class=\"textData\">Packages</th>");
		}
		if (needClasses) {
			builder.append("<th class=\"textData\">Classes</th>");
		}
		builder.append("<th class=\"textData\">Lines</th><th class=\"textData\">Conditionals</th></tr></thead><tbody>");

		return builder.toString();
	}

	private static String help = "<br><div class=\"information\">"
			+ "<p><b>Packages:</b>A package is considered as executed when at least one class has been executed.</p>"
			+ "<p><b>Classes:</b>A class is considered as executed when at least one of its lines has been executed. </p>"
			+ "<p><b>Lines:</b>A source line is considered executed when at least one instruction that is assigned to this line has been executed.</p>"
			+ "<p><b>Conditionals:</b>This metric counts the total number of such branches in a method and determines the number of executed or missed branches</p>"
			+ "</div>";

	/**
	 *
	 * /** Converts a type of coverage into HTML markup
	 *
	 * @param type
	 *            the type of coverage
	 * @param counters
	 *            the counter list
	 * @return the generated markup
	 */
	private static String convertToMarkup(Integer missed, Integer covered) {

		if (Integer.valueOf(0).equals(missed) && Integer.valueOf(0).equals(covered)) {
			return "<div style=\"text-align:center; line-height: 20px;\">N/A</div>";
		}

		return generateDiagramMarkup(missed, covered);
	}

	/**
	 * Generates one cell's HTML markup
	 *
	 * @param missed
	 *            missed number
	 * @param covered
	 *            covered number
	 * @param coveredPercent
	 *            coverage percentage
	 * @return cell markup
	 */
	private static String generateDiagramMarkup(Integer missed, Integer covered) {
		Integer all = missed + covered;
		Double percent = (covered / (double) all) * 100d;

		StringBuilder builder = new StringBuilder();
		builder.append("<div class=\"miniprogressbar\" style=\"width: 120px; height: 20px;\"> ");
		builder.append("<div style=\"width:");
		builder.append(percent.intValue());
		builder.append("%; background-color:#00A85D;\"></div>");
		builder.append("<div style=\"width:");
		builder.append(100 - percent.intValue());
		builder.append("%; background-color:#CC3F44;\">");
		builder.append("</div><div style=\"position: absolute; font-weight: bold; width: 100%; ");
		builder.append("background: transparent; text-align: center; color: white; line-height: 20px;\">");
		builder.append(String.format("%d/%d (%d%%)", covered, covered + missed, percent.intValue()));
		builder.append("</div></div>");
		return builder.toString();
	}

	public static String genearteSummary(CoverageBase base) {
		StringBuilder builder = new StringBuilder();

		builder.append("<h2><b>File Coverage Summary</b></h2>");
		builder.append(createHeader(false, false));

		builder.append("<tr>");
		builder.append("<td style=\"padding: 5px;\">" + base.getName() + ":</td>");
		appendColumns(builder, base, false);
		builder.append("</tr>");

		builder.append("</tbody></table>");

		builder.append(help);

		return builder.toString();
	}

	public static String genearteSummary(CoverageReport report) {
		StringBuilder builder = new StringBuilder();

		builder.append("<h2><b>Overall Coverage Summary</b></h2>");
		builder.append(createHeader(true, true));
		builder.append("<tr>");

		builder.append("<tr>");
		builder.append("<td style=\"padding: 5px;\">Cobertura Coverage Report:</td>");
		appendPackageColumn(builder, report);
		appendColumns(builder, report, true);
		builder.append("</tr></tbody></table>");

		builder.append(help);

		builder.append("<br><h2><b>Coverage Breakdown by Package</b></h2>");

		if (report.getDirectories() != null) {
			builder.append(createHeader(false, true));
			for (DirectoryCoverage directory : report.getDirectories()) {

				builder.append("<tr>");
				builder.append("<td style=\"padding: 5px;\">");
				builder.append(StringUtils.isBlank(directory.getName()) ? "default"
						: StringUtils.replace(directory.getName(), "/", "."));
				builder.append("</td>");

				appendColumns(builder, directory, true);
				builder.append("</tr>");
			}
			builder.append("</tbody></table>");
		}

		return builder.toString();
	}

	private static void appendPackageColumn(StringBuilder builder, CoverageReport report) {

		List<DirectoryCoverage> directories = report.getDirectories();

		Integer packageCovered = 0;
		Integer packageMissed = 0;

		if ((directories != null) && !directories.isEmpty()) {
			for (DirectoryCoverage directoryCoverage : directories) {
				if ((directoryCoverage.getClassCovered() != null)
						&& (directoryCoverage.getClassCovered().intValue() > 0)) {
					packageCovered++;
				} else {
					packageMissed++;
				}
			}
		}

		builder.append("<td style=\"padding: 5px;\">");
		builder.append(convertToMarkup(packageMissed, packageCovered));
		builder.append("</td>");
	}

	public static String genearteSummary(DirectoryCoverage directory) {

		StringBuilder builder = new StringBuilder();

		builder.append("<h2><b>Package Coverage Summary</b></h2>");
		builder.append(createHeader(false, true));

		builder.append("<tr>");
		builder.append("<td style=\"padding: 5px;\">" + directory.getName() + ":</td>");
		appendColumns(builder, directory, true);
		builder.append("</tr>");

		builder.append("</tbody></table>");

		builder.append(help);

		builder.append("<br><h2><b>Coverage Breakdown by Class</b></h2>");

		List<CoverageBase> files = directory.getFiles();
		if (files != null) {
			builder.append(createHeader(false, false));
			for (CoverageBase base : files) {

				builder.append("<tr>");
				builder.append("<td style=\"padding: 5px;\">" + base.getName() + ":</td>");
				appendColumns(builder, base, false);
				builder.append("</tr>");

			}
			builder.append("</tbody></table>");
		}

		return builder.toString();

	}

	private static void appendColumns(StringBuilder builder, CoverageBase base, boolean appendClass) {

		if (appendClass) {
			builder.append("<td style=\"padding: 5px;\">");
			builder.append(convertToMarkup(base.getClassMissed(), base.getClassCovered()));
			builder.append("</td>");
		}

		builder.append("<td style=\"padding: 5px;\">");
		builder.append(convertToMarkup(base.getLineMissed(), base.getLineCovered()));
		builder.append("</td>");

		builder.append("<td style=\"padding: 5px;\">");
		builder.append(convertToMarkup(base.getBranchMissed(), base.getBranchCovered()));
		builder.append("</td>");

	}
}
