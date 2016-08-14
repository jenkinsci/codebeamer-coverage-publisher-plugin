package com.intland.jenkins.coverage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.intland.jenkins.api.CodebeamerApiClient;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

/**
 * The execution context which hold the execution properties and the jenkins
 * build and listener classes
 *
 * @author abanfi
 */
public class ExecutionContext {

	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private final BuildListener listener;
	private final AbstractBuild<?, ?> build;
	private CodebeamerApiClient client;

	private String uri;
	private String jacocReportPath;
	private String coberturaReportPath;
	private String username;
	private String password;
	private Integer testSetTrackerId;
	private Integer testCaseTrackerId;
	private Integer testCaseParentId;
	private Integer testRunTrackerId;
	private Integer testConfigurationId;
	private String[] includedPackages;
	private String[] excludedPackages;
	private Integer successInstructionCoverage;
	private Integer successBranchCoverage;
	private Integer successComplexityCoverage;
	private Integer successLineCoverage;
	private Integer successMethodCoverage;
	private Integer successClassCoverage;
	private boolean testCaseTypeSupported;

	public ExecutionContext(BuildListener listener, AbstractBuild<?, ?> build) {
		this.listener = listener;
		this.build = build;
	}

	public File getRootDirectory() {
		try {
			return new File(this.build.getWorkspace().toURI());
		} catch (IOException | InterruptedException e) {
			this.log("Workspace root path cannot be resolved!");
		}
		return null;
	}

	public void logFormat(String message, Object... parameters) {
		this.log(String.format(message, parameters));
	}

	public void log(String message) {
		String log = String.format("%s %s", this.DATE_FORMAT.format(new Date()), message);
		this.listener.getLogger().println(log);
	}

	/**
	 * Returns the API client - and initializes it if it is not created yet
	 *
	 * @return
	 */
	public CodebeamerApiClient getClient() {
		if (this.client == null) {
			this.client = new CodebeamerApiClient(this.uri, this.username, this.password);
		}
		return this.client;
	}

	/**
	 * Returns a unique identifier for the current build (job name + # + build
	 * number)
	 *
	 * @return
	 */
	public String getBuildIdentifier() {
		return this.getJobName() + " #" + this.build.getNumber();
	}

	/**
	 * Get the current job's name
	 *
	 * @return
	 */
	public String getJobName() {
		return this.build.getProject().getName();
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getTestSetTrackerId() {
		return this.testSetTrackerId;
	}

	public void setTestSetTrackerId(Integer testSetTrackerId) {
		this.testSetTrackerId = testSetTrackerId;
	}

	public Integer getTestCaseTrackerId() {
		return this.testCaseTrackerId;
	}

	public void setTestCaseTrackerId(Integer testCaseTrackerId) {
		this.testCaseTrackerId = testCaseTrackerId;
	}

	public Integer getTestCaseParentId() {
		return this.testCaseParentId;
	}

	public void setTestCaseParentId(Integer testCaseParentId) {
		this.testCaseParentId = testCaseParentId;
	}

	public Integer getTestRunTrackerId() {
		return this.testRunTrackerId;
	}

	public void setTestRunTrackerId(Integer testRunTrackerId) {
		this.testRunTrackerId = testRunTrackerId;
	}

	public Integer getTestConfigurationId() {
		return this.testConfigurationId;
	}

	public void setTestConfigurationId(Integer testConfigurationId) {
		this.testConfigurationId = testConfigurationId;
	}

	public String[] getIncludedPackages() {
		return this.includedPackages;
	}

	public void setIncludedPackages(String[] includedPackages) {
		this.includedPackages = includedPackages;
	}

	public String[] getExcludedPackages() {
		return this.excludedPackages;
	}

	public void setExcludedPackages(String[] excludedPackages) {
		this.excludedPackages = excludedPackages;
	}

	public Integer getSuccessInstructionCoverage() {
		return this.successInstructionCoverage;
	}

	public void setSuccessInstructionCoverage(Integer successInstructionCoverage) {
		this.successInstructionCoverage = successInstructionCoverage;
	}

	public Integer getSuccessBranchCoverage() {
		return this.successBranchCoverage;
	}

	public void setSuccessBranchCoverage(Integer successBranchCoverage) {
		this.successBranchCoverage = successBranchCoverage;
	}

	public Integer getSuccessComplexityCoverage() {
		return this.successComplexityCoverage;
	}

	public void setSuccessComplexityCoverage(Integer successComplexityCoverage) {
		this.successComplexityCoverage = successComplexityCoverage;
	}

	public Integer getSuccessLineCoverage() {
		return this.successLineCoverage;
	}

	public void setSuccessLineCoverage(Integer successLineCoverage) {
		this.successLineCoverage = successLineCoverage;
	}

	public Integer getSuccessMethodCoverage() {
		return this.successMethodCoverage;
	}

	public void setSuccessMethodCoverage(Integer successMethodCoverage) {
		this.successMethodCoverage = successMethodCoverage;
	}

	public Integer getSuccessClassCoverage() {
		return this.successClassCoverage;
	}

	public void setSuccessClassCoverage(Integer successClassCoverage) {
		this.successClassCoverage = successClassCoverage;
	}

	public boolean isTestCaseTypeSupported() {
		return this.testCaseTypeSupported;
	}

	public void setTestCaseTypeSupported(boolean testCaseSupported) {
		this.testCaseTypeSupported = testCaseSupported;
	}

	public String getCoberturaReportPath() {
		return this.coberturaReportPath;
	}

	public void setCoberturaReportPath(String coberturaReportPath) {
		this.coberturaReportPath = coberturaReportPath;
	}

	public String getJacocReportPath() {
		return this.jacocReportPath;
	}

	public void setJacocReportPath(String jacocReportPath) {
		this.jacocReportPath = jacocReportPath;
	}

}
