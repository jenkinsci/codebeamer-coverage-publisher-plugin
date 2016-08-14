package com.intland.jenkins.coverage;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.intland.jenkins.api.CodebeamerApiClient;
import com.intland.jenkins.api.dto.TrackerDto;
import com.intland.jenkins.api.dto.TrackerItemDto;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

@SuppressWarnings("unchecked")
public class CodeBeamerCoveragePublisher extends Notifier {

	private String uri;
	private String jacocoReportPath;
	private String coberturaReportPath;
	private String username;
	private String password;
	private Integer testSetTrackerId;
	private Integer testCaseTrackerId;
	private Integer testCaseParentId;
	private Integer testRunTrackerId;
	private Integer testConfigurationId;
	private String includedPackages;
	private String excludedPackages;

	private Integer successInstructionCoverage;
	private Integer successBranchCoverage;
	private Integer successComplexityCoverage;
	private Integer successLineCoverage;
	private Integer successMethodCoverage;
	private Integer successClassCoverage;

	@DataBoundConstructor
	public CodeBeamerCoveragePublisher() {
		this.uri = StringUtils.EMPTY;
		this.username = StringUtils.EMPTY;
		this.password = StringUtils.EMPTY;
		this.testSetTrackerId = null;
		this.testCaseTrackerId = null;
		this.testCaseParentId = null;
		this.testRunTrackerId = null;
		this.testConfigurationId = null;
		this.includedPackages = StringUtils.EMPTY;
		this.excludedPackages = StringUtils.EMPTY;
		this.successInstructionCoverage = 0;
		this.successBranchCoverage = 0;
		this.successComplexityCoverage = 0;
		this.successLineCoverage = 0;
		this.successMethodCoverage = 0;
		this.successClassCoverage = 0;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher laucher, BuildListener listener)
			throws InterruptedException, IOException {

		// create the execution context
		ExecutionContext context = new ExecutionContext(listener, build);

		// construct configuration
		context.setUri(this.uri);
		context.setUsername(this.username);
		context.setPassword(this.password);
		context.setJacocReportPath(this.jacocoReportPath);
		context.setCoberturaReportPath(this.coberturaReportPath);
		context.setTestCaseParentId(this.testCaseParentId);
		context.setTestCaseTrackerId(this.testCaseTrackerId);
		context.setTestConfigurationId(this.testConfigurationId);
		context.setTestRunTrackerId(this.testRunTrackerId);
		context.setTestSetTrackerId(this.testSetTrackerId);
		context.setSuccessBranchCoverage(this.successBranchCoverage);
		context.setSuccessClassCoverage(this.successClassCoverage);
		context.setSuccessComplexityCoverage(this.successComplexityCoverage);
		context.setSuccessInstructionCoverage(this.successInstructionCoverage);
		context.setSuccessLineCoverage(this.successLineCoverage);
		context.setSuccessMethodCoverage(this.successMethodCoverage);
		context.setExcludedPackages(this.excludedPackages);
		context.setIncludedPackages(this.includedPackages);

		// execute the coverage
		CodebeamerCoverageExecutor.execute(context);

		// return true to prevent build faliure on plugin error
		return true;
	}

	public String getUri() {
		return this.uri;
	}

	@DataBoundSetter
	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUsername() {
		return this.username;
	}

	@DataBoundSetter
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	@DataBoundSetter
	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getTestSetTrackerId() {
		return this.testSetTrackerId;
	}

	@DataBoundSetter
	public void setTestSetTrackerId(Integer testSetTrackerId) {
		this.testSetTrackerId = testSetTrackerId;
	}

	public Integer getTestCaseTrackerId() {
		return this.testCaseTrackerId;
	}

	@DataBoundSetter
	public void setTestCaseTrackerId(Integer testCaseTrackerId) {
		this.testCaseTrackerId = testCaseTrackerId;
	}

	public Integer getTestCaseParentId() {
		return this.testCaseParentId;
	}

	@DataBoundSetter
	public void setTestCaseParentId(Integer testCaseParentId) {
		this.testCaseParentId = testCaseParentId;
	}

	public Integer getTestRunTrackerId() {
		return this.testRunTrackerId;
	}

	@DataBoundSetter
	public void setTestRunTrackerId(Integer testRunTrackerId) {
		this.testRunTrackerId = testRunTrackerId;
	}

	public Integer getTestConfigurationId() {
		return this.testConfigurationId;
	}

	@DataBoundSetter
	public void setTestConfigurationId(Integer testConfigurationId) {
		this.testConfigurationId = testConfigurationId;
	}

	public String getIncludedPackages() {
		return this.includedPackages;
	}

	@DataBoundSetter
	public void setIncludedPackages(String includedPackages) {
		this.includedPackages = includedPackages;
	}

	public String getExcludedPackages() {
		return this.excludedPackages;
	}

	@DataBoundSetter
	public void setExcludedPackages(String excludedPackages) {
		this.excludedPackages = excludedPackages;
	}

	@Extension
	public static final BuildStepDescriptor<Publisher> DESCRIPTOR = new DescriptorImpl();

	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public static final String PLUGIN_SHORTNAME = "codebeamer-coverage";

		@Override
		public String getDisplayName() {
			return "Codebeamer Coverage Plugin";
		}

		@Override
		public String getHelpFile() {
			return "/plugin/" + PLUGIN_SHORTNAME + "/help/help.html";
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		public FormValidation doCheckTestSetTrackerId(@QueryParameter Integer value, @QueryParameter String uri,
				@QueryParameter String username, @QueryParameter String password) throws IOException {
			return this.validateTrackerType(value, 108, uri, username, password, true);
		}

		public FormValidation doCheckTestCaseTrackerId(@QueryParameter Integer value, @QueryParameter String uri,
				@QueryParameter String username, @QueryParameter String password,
				@QueryParameter Integer testCaseParentId) throws IOException {
			if (testCaseParentId == null) {
				return this.validateTrackerType(value, 102, uri, username, password, true);
			} else {
				return FormValidation.ok();
			}
		}

		public FormValidation doCheckTestCaseParentId(@QueryParameter Integer value, @QueryParameter String uri,
				@QueryParameter String username, @QueryParameter String password) throws IOException {
			return this.validateTrackerItemWithTracker(value, uri, username, password, 102, false);
		}

		public FormValidation doCheckTestRunTrackerId(@QueryParameter Integer value, @QueryParameter String uri,
				@QueryParameter String username, @QueryParameter String password) throws IOException {
			return this.validateTrackerType(value, 9, uri, username, password, true);
		}

		public FormValidation doCheckTestConfigurationId(@QueryParameter Integer value, @QueryParameter String uri,
				@QueryParameter String username, @QueryParameter String password) throws IOException {
			return this.validateTrackerItemWithTracker(value, uri, username, password, 109, true);
		}

		private FormValidation validateTrackerItemWithTracker(Integer value, String uri, String userName,
				String password, int validTrackerTypeId, boolean required) {
			FormValidation result = FormValidation.ok();
			if (value != null) {
				try {
					CodebeamerApiClient apiClient = new CodebeamerApiClient(uri, userName, password);
					TrackerItemDto trackerItem = apiClient.getTrackerItem(value);
					if (trackerItem != null) {
						Integer trackerId = trackerItem.getTracker().getId();
						result = this.validateTrackerType(trackerId, validTrackerTypeId, uri, userName, password,
								false);
					} else {
						result = FormValidation.error("Tracker Item can not be found");
					}
				} catch (IOException e) {
					result = FormValidation.error("codeBeamer could not be reached with the provided uri/credentials");
				}
			} else if (required) {
				result = FormValidation.error("This field is required");
			}
			return result;
		}

		private FormValidation validateTrackerType(Integer value, int validTrackerTypeId, String uri, String userName,
				String password, boolean required) {
			FormValidation result = FormValidation.ok();

			if (value != null) {
				try {
					boolean valid = this.checkTrackerType(uri, userName, password, value, validTrackerTypeId);
					if (valid) {
						result = FormValidation.ok();
					} else {
						result = FormValidation.error("Tracker Type does not match the required Type");
					}
				} catch (IOException e) {
					result = FormValidation.error("codeBeamer could not be reached with the provided uri/credentials");
				}
			} else if (required) {
				result = FormValidation.error("This field is required");
			}

			return result;
		}

		private boolean checkTrackerType(String uri, String userName, String password, Integer trackerId,
				int validTrackerTypeId) throws IOException {
			CodebeamerApiClient apiClient = new CodebeamerApiClient(uri, userName, password);
			TrackerDto trackerDto = apiClient.getTracker(trackerId);
			boolean result = false;

			if (trackerDto != null) {
				Integer typeId = trackerDto.getType().getTypeId();
				result = (typeId != null) && (typeId.intValue() == validTrackerTypeId);
			}

			return result;
		}
	}

	public Integer getSuccessInstructionCoverage() {
		return this.successInstructionCoverage;
	}

	@DataBoundSetter
	public void setSuccessInstructionCoverage(Integer successInstructionCoverage) {
		this.successInstructionCoverage = successInstructionCoverage;
	}

	public Integer getSuccessBranchCoverage() {
		return this.successBranchCoverage;
	}

	@DataBoundSetter
	public void setSuccessBranchCoverage(Integer successBranchCoverage) {
		this.successBranchCoverage = successBranchCoverage;
	}

	public Integer getSuccessComplexityCoverage() {
		return this.successComplexityCoverage;
	}

	@DataBoundSetter
	public void setSuccessComplexityCoverage(Integer successComplexityCoverage) {
		this.successComplexityCoverage = successComplexityCoverage;
	}

	public Integer getSuccessLineCoverage() {
		return this.successLineCoverage;
	}

	@DataBoundSetter
	public void setSuccessLineCoverage(Integer successLineCoverage) {
		this.successLineCoverage = successLineCoverage;
	}

	public Integer getSuccessMethodCoverage() {
		return this.successMethodCoverage;
	}

	@DataBoundSetter
	public void setSuccessMethodCoverage(Integer successMethodCoverage) {
		this.successMethodCoverage = successMethodCoverage;
	}

	public Integer getSuccessClassCoverage() {
		return this.successClassCoverage;
	}

	@DataBoundSetter
	public void setSuccessClassCoverage(Integer successClassCoverage) {
		this.successClassCoverage = successClassCoverage;
	}

	public String getJacocoReportPath() {
		return this.jacocoReportPath;
	}

	@DataBoundSetter
	public void setJacocoReportPath(String jacocReportPath) {
		this.jacocoReportPath = jacocReportPath;
	}

	public String getCoberturaReportPath() {
		return this.coberturaReportPath;
	}

	@DataBoundSetter
	public void setCoberturaReportPath(String coberturaReportPath) {
		this.coberturaReportPath = coberturaReportPath;
	}

}
