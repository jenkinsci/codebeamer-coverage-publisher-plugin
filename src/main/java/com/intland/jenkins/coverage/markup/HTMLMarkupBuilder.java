package com.intland.jenkins.coverage.markup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.intland.jenkins.jacoco.model.Counter;
import com.intland.jenkins.jacoco.model.Method;
import com.intland.jenkins.jacoco.model.Package;
import com.intland.jenkins.jacoco.model.Report;

/**
 * HTML markup builder for a jacoco coverage result
 *
 * @author abanfi
 *
 */
public class HTMLMarkupBuilder {

	private static String header = "<table class=\"trackerItems relationsExpander displaytag treetable trackerItemTreeTable\" style=\"width: 300px;\">"
			+ "<thead><tr><th style=\"min-width: 200px;\" class=\"textData\">Element</th><th class=\"textData\">Instructions</th>"
			+ "<th class=\"textData\">Branches</th><th class=\"textData\">Complexity</th>"
			+ "<th class=\"textData\">Lines</th><th class=\"textData\">Methods</th>"
			+ "<th class=\"textData\">Classes</th></tr></thead><tbody>";

	private static String INSTRUCTION = "INSTRUCTION";
	private static String BRANCH = "BRANCH";
	private static String COMPLEXITY = "COMPLEXITY";
	private static String LINE = "LINE";
	private static String METHOD = "METHOD";
	private static String CLASS = "CLASS";

	private static String[] COLUMNS = new String[] { INSTRUCTION, BRANCH, COMPLEXITY, LINE, METHOD, CLASS };

	private static Set<String> WRITE_COUNT_COLUMNS = new HashSet<>(Arrays.asList(LINE, METHOD, CLASS));

	/**
	 * Generates a HTML markup for the specified class
	 *
	 * @param clazz
	 * @return the report markup
	 */
	public static String genearteSummary(com.intland.jenkins.jacoco.model.Class clazz) {

		StringBuilder builder = new StringBuilder();

		// total part
		builder.append("<h2><b>Overall coverage Summary</b></h2>");
		builder.append(header);
		builder.append("<tr>");
		appendTotal(builder, "all methods", clazz.getCounter());
		builder.append("</tr></tbody></table>");

		// method part
		builder.append("<br><h2><b>Coverage Breakdown by Method</b></h2>");

		if (clazz.getMethod() != null) {
			builder.append(
					StringUtils.replace(header, "Element", String.format("Element (%d)", clazz.getMethod().size())));
			for (Method method : clazz.getMethod()) {
				builder.append("<tr>");
				builder.append("<td>");
				builder.append(method.getName());
				builder.append("</td>");

				appendColumns(builder, method.getCounter());
				builder.append("</tr>");
			}
			builder.append("</tbody></table>");
		}

		return builder.toString();
	}

	/**
	 * Generates a HTML markup for the specified package
	 *
	 * @param pack
	 *            the package
	 * @return the report markup
	 */
	public static String genearteSummary(Package pack) {

		StringBuilder builder = new StringBuilder();

		// total part
		builder.append("<h2><b>Overall coverage Summary</b></h2>");
		builder.append(header);
		builder.append("<tr>");
		appendTotal(builder, "all classes", pack.getCounter());
		builder.append("</tr></tbody></table>");

		// classes part
		builder.append("<br><h2><b>Coverage Breakdown by Class</b></h2>");
		if (pack.getClazz() != null) {
			builder.append(
					StringUtils.replace(header, "Element", String.format("Element (%d)", pack.getClazz().size())));
			for (com.intland.jenkins.jacoco.model.Class clazz : pack.getClazz()) {

				builder.append("<tr>");
				builder.append("<td>");
				builder.append(StringUtils.substringAfterLast(clazz.getName(), "/"));
				builder.append("</td>");

				appendColumns(builder, clazz.getCounter());
				builder.append("</tr>");
			}
			builder.append("</tbody></table>");
		}

		return builder.toString();
	}

	/**
	 * Generates a HTML markup for the specified report
	 *
	 * @param pack
	 *            the report
	 * @return the report markup
	 */
	public static String genearteSummary(Report report) {

		StringBuilder builder = new StringBuilder();

		builder.append("<h2><b>Overall Coverage Summary</b></h2>");
		builder.append(header);
		builder.append("<tr>");
		appendTotal(builder, "all packages", report.getCounter());
		builder.append("</tr></tbody></table>");

		builder.append("<br><h2><b>Coverage Breakdown by Package</b></h2>");

		if (report.getPackage() != null) {
			builder.append(
					StringUtils.replace(header, "Element", String.format("Element (%d)", report.getPackage().size())));
			for (Package pack : report.getPackage()) {

				builder.append("<tr>");
				builder.append("<td>");
				builder.append(StringUtils.isBlank(pack.getName()) ? "default"
						: StringUtils.replace(pack.getName(), "/", "."));
				builder.append("</td>");

				appendColumns(builder, pack.getCounter());
				builder.append("</tr>");
			}
			builder.append("</tbody></table>");
		}

		return builder.toString();
	}

	/**
	 * Appends the total columns to the result
	 *
	 * @param builder
	 *            the string builder that hold the result
	 * @param label
	 *            the row label
	 * @param counters
	 *            the coverage counters
	 */
	private static void appendTotal(StringBuilder builder, String label, List<Counter> counters) {
		builder.append("<td>" + label + ":</td>");
		appendColumns(builder, counters);
	}

	/**
	 * Append counter values to the row
	 *
	 * @param builder
	 *            the string builder that hold the result
	 * @param counter
	 *            the coverage counters
	 */
	private static void appendColumns(StringBuilder builder, List<Counter> counter) {
		for (String column : COLUMNS) {
			builder.append("<td>");
			builder.append(convertToMarkup(column, counter));
			builder.append("</td>");
		}
	}

	/**
	 * Converts a type of coverage into HTML markup
	 *
	 * @param type
	 *            the type of coverage
	 * @param counters
	 *            the counter list
	 * @return the generated markup
	 */
	private static String convertToMarkup(String type, List<Counter> counters) {

		for (Counter counter : counters) {
			// only one counter exist with the required type
			if (counter.getType().equals(type)) {
				Integer all = counter.getMissed() + counter.getCovered();
				Double percent = (counter.getCovered() / (double) all) * 100d;
				return generateDiagramMarkup(WRITE_COUNT_COLUMNS.contains(type), counter.getMissed(),
						counter.getCovered(), percent.intValue());
			}
		}

		return "<div style=\"text-align:center; line-height: 20px;\">N/A</div>";
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
	private static String generateDiagramMarkup(boolean writeOutCounts, Integer missed, Integer covered,
			Integer coveredPercent) {
		StringBuilder builder = new StringBuilder();
		builder.append("<div class=\"miniprogressbar\" style=\"width: 120px; height: 20px;\"> ");
		builder.append("<div style=\"width:");
		builder.append(coveredPercent);
		builder.append("%; background-color:#00A85D;\"></div>");
		builder.append("<div style=\"width:");
		builder.append(100 - coveredPercent);
		builder.append("%; background-color:#CC3F44;\">");
		builder.append("</div><div style=\"position: absolute; font-weight: bold; width: 100%; ");
		builder.append("background: transparent; text-align: center; color: white; line-height: 20px;\">");
		if (writeOutCounts) {
			builder.append(String.format("%d/%d (%d%%)", covered + missed, missed, coveredPercent));
		} else {
			builder.append(coveredPercent);
			builder.append("%");
		}
		builder.append("</div></div>");
		return builder.toString();
	}
}
