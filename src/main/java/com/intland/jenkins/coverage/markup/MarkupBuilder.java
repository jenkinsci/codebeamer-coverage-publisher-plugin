package com.intland.jenkins.coverage.markup;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.intland.jenkins.jacoco.model.Counter;
import com.intland.jenkins.jacoco.model.Method;
import com.intland.jenkins.jacoco.model.Package;
import com.intland.jenkins.jacoco.model.Report;

/**
 * Deprecated - use HTMLMarkupBuilder
 *
 * @author abanfi
 *
 */
@Deprecated()
public class MarkupBuilder {
	private static final String COLUMN_START = "\n\n|";

	private static final String DIAGRAM_MARKUP_TEMPLATE = "diagram-markup-3.template";

	private static String header = "[{Table \n\n|| Element \n|| Instructions \n|| Branches \n|| Complexity \n|| Lines \n|| Methods \n|| Classes \n";

	private static String INSTRUCTION = "INSTRUCTION";
	private static String BRANCH = "BRANCH";
	private static String COMPLEXITY = "COMPLEXITY";
	private static String LINE = "LINE";
	private static String METHOD = "METHOD";
	private static String CLASS = "CLASS";

	private static String[] COLUMNS = new String[] { INSTRUCTION, BRANCH, COMPLEXITY, LINE, METHOD, CLASS };

	/**
	 * The diagram markup template from diagram-markup.template file
	 */
	private static String diagramMarkup;

	static {
		try (InputStream stream = MarkupBuilder.class.getResourceAsStream(DIAGRAM_MARKUP_TEMPLATE)) {
			diagramMarkup = IOUtils.toString(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String genearteSummary(com.intland.jenkins.jacoco.model.Class base) {

		StringBuilder builder = new StringBuilder();
		builder.append(header);

		for (Method method : base.getMethod()) {
			builder.append(COLUMN_START);
			builder.append(method.getName());

			appendColumns(builder, method.getCounter());
		}

		appendTotal(builder, base.getCounter());

		builder.append("\n\n}]");
		return builder.toString();
	}

	public static String genearteSummary(Package pack) {

		StringBuilder builder = new StringBuilder();
		builder.append(header);

		for (com.intland.jenkins.jacoco.model.Class clazz : pack.getClazz()) {
			builder.append(COLUMN_START);
			builder.append(StringUtils.substringAfterLast(clazz.getName(), "/"));

			appendColumns(builder, clazz.getCounter());
		}

		appendTotal(builder, pack.getCounter());

		builder.append("\n\n}]");
		return builder.toString();
	}

	public static String genearteSummary(Report report) {

		StringBuilder builder = new StringBuilder();
		builder.append(header);

		for (Package pack : report.getPackage()) {
			builder.append(COLUMN_START);
			builder.append(
					StringUtils.isBlank(pack.getName()) ? "default" : StringUtils.replace(pack.getName(), "/", "."));

			appendColumns(builder, pack.getCounter());
		}

		appendTotal(builder, report.getCounter());

		builder.append("\n\n}]");
		return builder.toString();
	}

	private static void appendTotal(StringBuilder builder, List<Counter> counters) {
		builder.append(COLUMN_START + "TOTAL:");

		for (String column : COLUMNS) {
			builder.append("|");
			builder.append(calculate(column, counters));
		}
	}

	private static void appendColumns(StringBuilder builder, List<Counter> counter) {
		for (String column : COLUMNS) {
			builder.append("|");
			builder.append(calculate(column, counter));
		}
	}

	private static String calculate(String type, List<Counter> counters) {

		for (Counter counter : counters) {
			if (counter.getType().equals(type)) {
				Integer all = counter.getMissed() + counter.getCovered();
				Double percent = (counter.getCovered() / (double) all) * 100d;
				return generateDiagramMarkup(counter.getMissed(), counter.getCovered(), percent.intValue());
			}
		}

		return "%%( text-align:center )N/A%%";
	}

	private static String generateDiagramMarkup(Integer missed, Integer covered, Integer coveredPercent) {
		// TODO add titles
		return String.format(diagramMarkup, missed, covered, coveredPercent, 100 - coveredPercent, coveredPercent);
	}
}
