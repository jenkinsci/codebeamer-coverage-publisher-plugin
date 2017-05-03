package com.intland.jenkins.coverage;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.intland.jenkins.api.CodebeamerApiClient;
import com.intland.jenkins.api.dto.*;
import com.intland.jenkins.coverage.model.CoverageBase;
import com.intland.jenkins.coverage.model.CoverageReport;
import com.intland.jenkins.coverage.model.CoverageReport.CoverageType;
import com.intland.jenkins.coverage.model.DirectoryCoverage;
import com.intland.jenkins.gcovr.GcovResultParser;
import com.intland.jenkins.jacoco.JacocoResultParser;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CodebeamerCoverageExecutor {

	private static final String TEST_CASE_TYPE_NAME = "Automated";
	private static final String SUCCESS_STATUS = "Passed";
	private static final String FAILED_STATUS = "Failed";
	private static final String DEFAULT_TESTSET_NAME = "Jenkins-Coverage";

	/**
	 * Executes the codebeamer coverage registration process
	 *
	 * @param context
	 *            the context of the execution {@link ExecutionContext}
	 * 
	 */
	public static void execute(ExecutionContext context) throws IOException {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		context.log("Start to process coverage report.");

		// get and check the report
		List<CoverageReport> reports = loadReport(context);
		stopWatch.stop();

		if ((reports == null) || reports.isEmpty()) {
			context.log("Report cannot be parsed or cannot be found! Execution stopping.");
			return;
		}

		for (CoverageReport report : reports) {
			context.logFormat("%s Parsing finished in %d ms!", report.toSummary(), stopWatch.getTime());

			context.logFormat("Filtering packages (%d packages found by default)", report.getDirectories().size());
			filterPackages(context, report);
			context.logFormat("%d packages in report after filtering", report.getDirectories().size());

			CodebeamerApiClient client = context.getClient();

			context.log("Checking supported test case types...");
			boolean isTestCaseTypeSupported = client.isTestCaseTypeSupported(context.getTestCaseTrackerId(),
					TEST_CASE_TYPE_NAME);
			context.setTestCaseTypeSupported(isTestCaseTypeSupported);
			context.logFormat("Test Case type: %s, supported: %s", TEST_CASE_TYPE_NAME, isTestCaseTypeSupported);

			context.log("Load existing test cases.");
			List<TrackerItemDto> testCases = client.getTestCaseList(context);
			context.logFormat("%d test cases found in tracker %d", testCases.size(), context.getTestCaseTrackerId());

			context.log("Collect test case ids.");
			Map<String, Integer> testCasesForCurrentResults = collectTestCaseIds(report, testCases, context);
			context.logFormat("%d Test Case items are collected.", testCasesForCurrentResults.size());

			context.log("Create or get default test set for coverage.");
			TrackerItemDto coverageTestSet = getOrCreateTestSet(context);
			context.logFormat("Coverage test set found: <%d>.", coverageTestSet.getId());

			context.log("Create new Test Run.");
			TrackerItemDto coverageTestRun = createTestRun(report, testCasesForCurrentResults, coverageTestSet,
					context);
			context.logFormat(" Test Run created: <%s>.", coverageTestRun.getId());

			context.log("Upload coverage result to the test run.");
			uploadResults(report, testCasesForCurrentResults, coverageTestSet, coverageTestRun, context);
			context.log("Uploading coverage result is completed!");
		}
	}

	/**
	 * Executes include or exclude filters
	 *
	 * @param context
	 * @param report
	 */
	private static void filterPackages(ExecutionContext context, CoverageReport report) {
		if (context.isIncludePackagesSpecified()) {
			Set<String> includedPackages = context.getIncludePackagesSet();

			if (includedPackages.size() > 0) {

				context.logFormat("Execute inclusion filter: <{}>", includedPackages);

				List<DirectoryCoverage> acceptedCoverages = new ArrayList<>();

				for (DirectoryCoverage directory : report.getDirectories()) {
					for (String packageName : includedPackages) {
						if (StringUtils.startsWith(directory.getName(), packageName)) {
							context.logFormat("Package included: %s", directory.getName());
							acceptedCoverages.add(directory);
							break;
						}
					}
				}

				report.setDirectories(acceptedCoverages);
				return;
			}
		}

		if (context.isIncludePackagesSpecified()) {
			Set<String> excludedPackages = context.getExcludePackageSet();

			if (excludedPackages.size() > 0) {

				context.logFormat("Execute exclusion filter: <%s>", excludedPackages);

				Iterator<DirectoryCoverage> iterator = report.getDirectories().iterator();
				while (iterator.hasNext()) {
					DirectoryCoverage directory = iterator.next();
					for (String packageName : excludedPackages) {
						if (StringUtils.startsWith(directory.getName(), packageName)) {
							context.logFormat("Package excluded: %s", directory.getName());
							iterator.remove();
							break;
						}
					}
				}
			}
		}

	}

	/**
	 * This method will iterate over all report result and create and upload a
	 * new test case run for each result entry using the specified properties
	 *
	 * @param report
	 *            the full coverage report
	 * @param testCasesForCurrentResults
	 *            a test case id / report entry name map
	 * @param coverageTestSet
	 *            the coverage test set
	 * @param coverageTestRun
	 *            the parent test run
	 * @param context
	 *            the execution context
	 * 
	 */
	private static void uploadResults(CoverageReport report, Map<String, Integer> testCasesForCurrentResults,
			TrackerItemDto coverageTestSet, TrackerItemDto coverageTestRun, ExecutionContext context)
			throws IOException {

		Integer testConfigurationId = context.getTestConfigurationId();

		// iterate over the coverage report result
		for (DirectoryCoverage directory : report.getDirectories()) {

			// create test for directory (package in java)
			Integer testCaseId = testCasesForCurrentResults.get(directory.getName());

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			context.logFormat("Create Test Run for directory : <%s>", directory.getName());
			createTestCaseRun(testConfigurationId, coverageTestSet, coverageTestRun, directory, testCaseId, context);

			stopWatch.stop();
			context.logFormat("Test run succesfully created in %d ms", stopWatch.getTime());

			// create test for file items (classes in java)
			for (CoverageBase fileCoverage : directory.getFiles()) {

				stopWatch = new StopWatch();
				stopWatch.start();
				context.logFormat("Create Test Run for file : <%s>", fileCoverage.getName());
				testCaseId = testCasesForCurrentResults.get(fileCoverage.getName());

				// FIXME just for demo, it is clearly wrong in here
				if (CoverageType.JACOCO.equals(report.getType())) {
					appendJenkinsUrl(fileCoverage, directory, context);
				}
				createTestCaseRun(testConfigurationId, coverageTestSet, coverageTestRun, fileCoverage, testCaseId,
						context);

				stopWatch.stop();
				context.logFormat("Test run succesfully created in %d ms", stopWatch.getTime());
			}
		}

		updateTestSetTestCasesAndStatus(coverageTestSet, testCasesForCurrentResults.values(), "Finished", context);
		TestCaseDto testCaseDto = new TestCaseDto(coverageTestRun.getId(), "Finished");
		context.getClient().put(context, testCaseDto);
	}

	/**
	 * Appends a generated jenkins URL which points to the rendered source code
	 * coverage of the specified covergaeBase
	 *
	 * @param coverageData
	 * @param parentCoverage
	 * @param context
	 */
	private static void appendJenkinsUrl(CoverageBase coverageData, DirectoryCoverage parentCoverage,
			ExecutionContext context) {
		String jenkinsBase = Jenkins.getInstance().getRootUrl();
		if (StringUtils.isNotBlank(jenkinsBase)) {
			StringBuilder builder = new StringBuilder();
			builder.append("<br><br><a class=\"actionLink\" href=\"");
			builder.append(jenkinsBase);
			builder.append((jenkinsBase.endsWith("/") ? "" : "/") + "job/");
			builder.append(context.getJobName());
			builder.append("/ws/");

			String reportPath = StringUtils.replace(context.getJacocReportPath(), "\\", "/");
			reportPath = StringUtils.substring(reportPath, 0, reportPath.lastIndexOf("/"));
			builder.append(reportPath);
			builder.append("/");
			builder.append(parentCoverage.getName());
			builder.append("/");
			builder.append(StringUtils.substringBeforeLast(coverageData.getName(), "$"));
			builder.append(".java.html\">Source Coverage</a>");

			coverageData.setMarkup(coverageData.getMarkup() + builder.toString());
		}

	}

	/**
	 * Updates the specified tracker item status and test case references
	 *
	 * @param coverageTestSet
	 *            the test set
	 * @param status
	 *            the new status
	 * @param context
	 *            the execution context
	 * 
	 */
	private static void updateTestSetTestCasesAndStatus(TrackerItemDto coverageTestSet, Collection<Integer> testCases,
			String status, ExecutionContext context) throws IOException {
		List<Object[]> testCasesList = new ArrayList<>();
		for (Integer testCaseId : testCases) {
			testCasesList.add(new Object[] { new ReferenceDto("/item/" + testCaseId), Boolean.TRUE, Boolean.TRUE });
		}

		TrackerItemDto trackerItemDto = new TrackerItemDto();
		trackerItemDto.setUri(coverageTestSet.getUri());
		trackerItemDto.setStatus(new NamedDto(status));
		trackerItemDto.setTestCases(testCasesList);

		context.getClient().put(context, trackerItemDto);
	}

	/**
	 * Creates a new test case run using the specified values
	 *
	 * @param testConfigurationId
	 *            the ID of the assigned test configuration
	 * @param coverageTestSet
	 *            the coverage test set
	 * @param coverageTestRun
	 *            the coverage test run, the parent of this test run
	 * @param coverageBase
	 *            the coverage report
	 * @param testCaseId
	 *            the related test case
	 * @param context
	 *            the execution context
	 * 
	 */
	private static void createTestCaseRun(Integer testConfigurationId, TrackerItemDto coverageTestSet,
			TrackerItemDto coverageTestRun, CoverageBase coverageBase, Integer testCaseId, ExecutionContext context)
			throws IOException {

		Integer coverageTestSetId = coverageTestSet == null ? null : coverageTestSet.getId();
		Integer coverageTestRunId = coverageTestRun == null ? null : coverageTestRun.getId();

		context.logFormat("Create a new test case run: config:<%d> testSet:<%d> testRun:<%d> testCase:<%d>",
				testConfigurationId, coverageTestSetId, coverageTestRunId, testCaseId);
		Integer testRunTrackerId = context.getTestRunTrackerId();

		// create test case run
		TestRunDto testRunDto = new TestRunDto(coverageBase.getName(), coverageTestRunId, testRunTrackerId,
				Arrays.asList(new Integer[] { testCaseId }), testConfigurationId,
				calculateStatus(coverageBase, context));
		testRunDto.setDescription(coverageBase.getMarkup());
		testRunDto.setDescFormat("Html");
		testRunDto.setTestSet(coverageTestSetId);
		context.logFormat("Generated markup character count: <%d>", coverageBase.getMarkup().length());

		// create test run item
		TrackerItemDto testRunItem = context.getClient().postTrackerItem(context, testRunDto);

		// update test case run to completed -
		TestCaseDto testCaseDto = new TestCaseDto(testRunItem.getId(), "Finished");
		testCaseDto.setSpentMillis(0l);
		context.getClient().put(context, testCaseDto);
	}

	/**
	 * Calculates the coverage status for the specified coverage base object
	 * using the build configuration
	 *
	 * @param coverageBase
	 *            the coverage object to evaluate
	 * @param context
	 *            the plugin's context configuration
	 * @return the coverage status as string (Passed or Failed)
	 */
	private static String calculateStatus(CoverageBase coverageBase, ExecutionContext context) {

		boolean result = true;
		result &= checkStatus(context.getSuccessBranchCoverage(), coverageBase.getBranchCovered(),
				coverageBase.getBranchMissed());
		result &= checkStatus(context.getSuccessClassCoverage(), coverageBase.getClassCovered(),
				coverageBase.getClassMissed());
		result &= checkStatus(context.getSuccessComplexityCoverage(), coverageBase.getComplexityCovered(),
				coverageBase.getComplexityMissed());
		result &= checkStatus(context.getSuccessInstructionCoverage(), coverageBase.getInstructionCovered(),
				coverageBase.getInstructionMissed());
		result &= checkStatus(context.getSuccessLineCoverage(), coverageBase.getLineCovered(),
				coverageBase.getLineMissed());
		result &= checkStatus(context.getSuccessMethodCoverage(), coverageBase.getMethodCovered(),
				coverageBase.getMethodMissed());

		return result ? SUCCESS_STATUS : FAILED_STATUS;
	}

	/**
	 * Calculates the percent from the given missed and covered count value and
	 * returns this percent is bigger or smaller than the limit
	 *
	 * @param successLimit
	 *            the limit
	 * @param covered
	 *            covered count
	 * @param missed
	 *            missed count
	 * @return true, if the coverage percentage is above the limit, otherwise
	 *         false
	 */
	private static boolean checkStatus(Integer successLimit, Integer covered, Integer missed) {

		// if the coverage is not calculated for this category
		if ((covered == null) || (missed == null)) {
			return true;
		}

		Integer all = covered + missed;
		Double percent = (covered / (double) all) * 100d;

		return successLimit <= percent.intValue();
	}

	/**
	 * Creates a new test run which assigned to the current build
	 *
	 * @param report
	 *            the coverage report that contains the report markup
	 * @param testCasesForCurrentResults
	 *            a name - id key value map that hold all of the test case in
	 *            the configured test case tracker
	 * @param testSetDto
	 * @param context
	 * @return
	 * 
	 */
	private static TrackerItemDto createTestRun(CoverageReport report, Map<String, Integer> testCasesForCurrentResults,
			TrackerItemDto testSetDto, ExecutionContext context) throws IOException {

		Integer testRunTrackerId = context.getTestRunTrackerId();
		Integer testConfigurationId = context.getTestConfigurationId();

		context.logFormat(
				"Creating Test run in test run tracker <%s>, with test configuration <%s> and added <%s> test cases.",
				testRunTrackerId, testConfigurationId, testCasesForCurrentResults.size());

		TestRunDto testRunDto = new TestRunDto(context.getBuildIdentifier(), null, testRunTrackerId,
				testCasesForCurrentResults.values(), testConfigurationId, calculateStatus(report, context));

		testRunDto.setTestSet(testSetDto.getId());
		testRunDto.setDescription(report.getMarkup());
		testRunDto.setDescFormat("Html");
		return context.getClient().postTrackerItem(context, testRunDto);
	}

	/**
	 * Creates or returns a test set assigned to the current build
	 *
	 * @param context
	 *            the context of the execution {@link ExecutionContext}
	 * @return the test set for the current build
	 * 
	 */
	private static TrackerItemDto getOrCreateTestSet(ExecutionContext context) throws IOException {

		String name = DEFAULT_TESTSET_NAME + "-" + context.getBuildIdentifier();
		Integer testSetTrackerId = context.getTestSetTrackerId();

		return context.getClient().findOrCreateTestSet(context, testSetTrackerId, name, "--");
	}

	/**
	 * Collect test case id's to the report's results
	 *
	 * @param report
	 *            the coverage result model {@link CoverageReport}
	 * @param testCases
	 *            list of the exsisting test cases in the configured test case
	 *            tracker
	 * @param context
	 *            the context of the execution {@link ExecutionContext}
	 * @return a name - test case id map
	 * 
	 */
	private static Map<String, Integer> collectTestCaseIds(CoverageReport report, List<TrackerItemDto> testCases,
			ExecutionContext context) throws IOException {

		// create a test case map by id
		ImmutableMap<Integer, TrackerItemDto> testCasesMapById = Maps.uniqueIndex(testCases,
				new Function<TrackerItemDto, Integer>() {

					@Override
					public Integer apply(TrackerItemDto itemDto) {
						return itemDto != null ? itemDto.getId() : null;
					}
				});

		// resolve test case root node if it is exists
		TrackerItemDto parent = null;
		Integer parentId = context.getTestCaseParentId();
		if ((parentId != null) && testCasesMapById.containsKey(parentId)) {
			parent = testCasesMapById.get(parentId);
			context.logFormat("Parent Test Case can be resolved by id: <%d>", parentId);
		}

		// log the parent is not found
		if (parent == null) {
			context.log("No parent node is found. The test caase root will be the tracker root node.");
		}

		// collect test cases in a map by canonical name
		Iterator<TrackerItemDto> iterator = testCases.iterator();
		Map<String, TrackerItemDto> testCasesMapByName = new HashMap<>();
		while (iterator.hasNext()) {
			TrackerItemDto trackerItemDto = iterator.next();

			if (isChildOf(parent, trackerItemDto, testCasesMapById)) {
				String canonicalName = calculateCanonicalName(parent, trackerItemDto, testCasesMapById);
				testCasesMapByName.put(canonicalName, trackerItemDto);
			}

		}

		Map<String, Integer> testCaseMap = new HashMap<>();

		// resolve ids or create new test cases
		for (DirectoryCoverage directory : report.getDirectories()) {

			// get test case for directory (package in java)
			String name = directory.getName();
			TrackerItemDto testCaseDto = searchForOrCreateTestCase(name, parent, context, testCasesMapByName);
			testCaseMap.put(name, testCaseDto.getId());

			// get test case for file items (classes in java)
			for (CoverageBase fileCoverage : directory.getFiles()) {
				String fileName = fileCoverage.getName();
				TrackerItemDto fileTestCaseDto = searchForOrCreateTestCase(name + "." + fileName, testCaseDto, context,
						testCasesMapByName);

				testCaseMap.put(fileName, fileTestCaseDto.getId());
			}
		}

		return testCaseMap;
	}

	/**
	 * Calculates the canonical name of the specified test case
	 *
	 * @param exportRootTestCase
	 *            the export root test case
	 * @param testCaseDto
	 *            the test case to check
	 * @param testCasesMapById
	 *            all test case mapped by id
	 * @return
	 */
	private static String calculateCanonicalName(TrackerItemDto exportRootTestCase, TrackerItemDto testCaseDto,
			ImmutableMap<Integer, TrackerItemDto> testCasesMapById) {

		// if the export root node is null and the item's parent is null then
		// return the item name
		if ((testCaseDto.getParent() == null) && (exportRootTestCase == null)) {
			return testCaseDto.getName();
		}

		// if the parent is reached
		Integer itemParentId = testCaseDto.getParent().getId();
		if ((exportRootTestCase != null) && itemParentId.equals(exportRootTestCase.getId())) {
			return testCaseDto.getName();
		}

		// call the method to the parent
		return calculateCanonicalName(exportRootTestCase, testCasesMapById.get(itemParentId), testCasesMapById) + "."
				+ testCaseDto.getName();
	}

	/**
	 * Recursively check the test case is the parent of the given sub test case
	 *
	 * @param exportRootTestCase
	 *            the export root test case
	 * @param testCaseDto
	 *            the test case to check
	 * @param testCasesMapById
	 *            all test case mapped by id
	 * @return
	 */
	private static boolean isChildOf(TrackerItemDto exportRootTestCase, TrackerItemDto testCaseDto,
			ImmutableMap<Integer, TrackerItemDto> testCasesMapById) {

		// every node is the child of the root (null) node
		if (exportRootTestCase == null) {
			return true;
		}

		// we reach a top node without reach the specified parent node as an
		// anchestor - the condition is false
		if (testCaseDto.getParent() == null) {
			return false;
		}

		// if the parent id is equal the specified parent id the condition is
		// true
		Integer itemParentId = testCaseDto.getParent().getId();
		if (itemParentId.equals(exportRootTestCase.getId())) {
			return true;
		}

		// call the method to the parent
		return isChildOf(exportRootTestCase, testCasesMapById.get(itemParentId), testCasesMapById);
	}

	/**
	 * Search in the specified test case map for a test case with the specified
	 * name and the specified parent. Returns the tracker item if it can be
	 * found in the map otherwise create a new one and returns with that
	 *
	 * @param name
	 *            the test case name
	 * @param parent
	 *            the test case parent item or null, if it is a root node
	 * @param context
	 *            the context of the execution {@link ExecutionContext}
	 * @param testCasesMapByName
	 *            existing test cases from the test case which set by the
	 *            configuration mapped by name
	 * @return the tracker item dto for the name
	 * 
	 */
	private static TrackerItemDto searchForOrCreateTestCase(String name, TrackerItemDto parent,
			ExecutionContext context, Map<String, TrackerItemDto> testCasesMapByName) throws IOException {

		if (testCasesMapByName.containsKey(name)) {

			// get test case item from the map
			TrackerItemDto trackerItemDto = testCasesMapByName.get(name);
			context.logFormat("Test case with name <%s> is already exist: <%s>", name, trackerItemDto.getId());
			return trackerItemDto;
		}

		return createNewTestCase(name, parent, testCasesMapByName, context);
	}

	/**
	 * Creates and returns a new test case in the test case tracker which set in
	 * the configuration
	 *
	 * @param name
	 *            the name of the new test case
	 * @param parentTestCase
	 *            the parent URI (eg. /item/{id}) of the parent test case or
	 *            null if the test case should create in the root
	 * @param testCasesMapByName
	 * @param context
	 *            the context of the execution {@link ExecutionContext}
	 * @return the created test case as a tracker item
	 * 
	 */
	private static TrackerItemDto createNewTestCase(String name, TrackerItemDto parentTestCase,
			Map<String, TrackerItemDto> testCasesMapByName, ExecutionContext context) throws IOException {

		Integer testCaseTrackerId = context.getTestCaseTrackerId();

		// split name parts - a name could be eg. a.b.c if the test case is
		// represent a directory or package
		String[] nameParts = StringUtils.split(name, ".");
		Integer parentId = parentTestCase == null ? null : parentTestCase.getId();
		String canonicalName = null;
		TrackerItemDto newTackerItem = null;

		// iterate over the parts
		for (String part : nameParts) {

			canonicalName = (canonicalName == null ? "" : canonicalName + ".") + part;
			if (testCasesMapByName.containsKey(canonicalName)) {
				TrackerItemDto trackerItemDto = testCasesMapByName.get(canonicalName);
				parentId = trackerItemDto.getId();
				continue;
			}

			// TODO create normal test case DTO
			TestRunDto testCaseDto = new TestRunDto(part, "/tracker/" + testCaseTrackerId, parentId);
			testCaseDto.setDescription("--");
			if (context.isTestCaseTypeSupported()) {
				testCaseDto.setType(TEST_CASE_TYPE_NAME);
			}

			// create the tracker item and log the result
			newTackerItem = context.getClient().postTrackerItem(context, testCaseDto);
			context.logFormat("New test case <%d> created in tracker <%s> with parent <%s>", newTackerItem.getId(),
					testCaseTrackerId, parentId);

			// update status to accepted
			context.getClient().updateTestCaseStatus(context, newTackerItem.getId(), "Accepted");

			parentId = newTackerItem.getId();

			// update the test case map
			testCasesMapByName.put(canonicalName, newTackerItem);
		}

		// return the last test case
		return newTackerItem;
	}

	/**
	 * Loads report by the given context
	 *
	 * @param context
	 *            the context of the execution {@link ExecutionContext}
	 * @return the common coverage report result {@link CoverageReport}
	 * 
	 */
	private static List<CoverageReport> loadReport(ExecutionContext context) throws IOException {

		List<CoverageReport> reports = new ArrayList<>();

		// check and load jacoco report
		context.log("Check Jacoco coverage");
		if (checkReportFile(context, context.getJacocReportPath())) {
			File reportFile = new File(context.getRootDirectory(), context.getJacocReportPath());
			reports.add(new JacocoResultParser().collectCoverageReport(reportFile.getAbsolutePath(), context));
		}

		// check and load cobertura report
		context.log("Check Coburtura coverage");
		if (checkReportFile(context, context.getCoberturaReportPath())) {
			File reportFile = new File(context.getRootDirectory(), context.getCoberturaReportPath());
			reports.add(new GcovResultParser().collectCoverageReport(reportFile.getAbsolutePath(), context));
		}

		return reports;
	}

	/**
	 * Checks the specified report file exists in workspace
	 *
	 * @param context
	 * @param reportPath
	 * @return
	 */
	private static boolean checkReportFile(ExecutionContext context, String reportPath) {
		if (StringUtils.isNotBlank(reportPath)) {
			File rootDirectory = context.getRootDirectory();
			File reportFile = new File(rootDirectory, reportPath);

			// validate report file - it should be exists
			if (!reportFile.exists()) {
				context.log(String.format("Report file cannot be found at path <%s>", reportFile.getAbsolutePath()));
				return false;
			}

			context.log(String.format("Report file found at <%s> with length <%d>", reportPath, reportFile.length()));
			return true;
		}
		return false;
	}

}
