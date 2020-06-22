package de.tum.in.www1.artemis.service.connectors.bamboo.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BambooBuildResultNotificationDTO {

    private String secret;

    private String notificationType;

    private BambooBuildDTO build;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public BambooBuildDTO getBuild() {
        return build;
    }

    public void setBuild(BambooBuildDTO build) {
        this.build = build;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class BambooBuildDTO {

        private boolean artifact;

        private int number;

        private String reason;

        private ZonedDateTime buildCompletedDate;

        private boolean successful;

        private BambooTestSummaryDTO testSummary;

        private List<BambooVCSDTO> vcs;

        private List<BambooJobDTO> jobs;

        public boolean isArtifact() {
            return artifact;
        }

        public void setArtifact(boolean artifact) {
            this.artifact = artifact;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public ZonedDateTime getBuildCompletedDate() {
            return buildCompletedDate;
        }

        public void setBuildCompletedDate(ZonedDateTime buildCompletedDate) {
            this.buildCompletedDate = buildCompletedDate;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public void setSuccessful(boolean successful) {
            this.successful = successful;
        }

        public BambooTestSummaryDTO getTestSummary() {
            return testSummary;
        }

        public void setTestSummary(BambooTestSummaryDTO testSummary) {
            this.testSummary = testSummary;
        }

        public List<BambooVCSDTO> getVcs() {
            return vcs;
        }

        public void setVcs(List<BambooVCSDTO> vcs) {
            this.vcs = vcs;
        }

        public List<BambooJobDTO> getJobs() {
            return jobs;
        }

        public void setJobs(List<BambooJobDTO> jobs) {
            this.jobs = jobs;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class BambooTestSummaryDTO {

        // We don't even know what unit this. It's doesn't align at all with the value displayed in Bamboo.
        // E.g. we got a value of 246 for an 8 second run?
        private int duration;

        private int ignoreCount;

        private int failedCount;

        private int existingFailedCount;

        private int quarantineCount;

        private int successfulCount;

        private String description;

        private int skippedCount;

        private int fixedCount;

        private int totalCount;

        private int newFailedCount;

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getIgnoreCount() {
            return ignoreCount;
        }

        public void setIgnoreCount(int ignoreCount) {
            this.ignoreCount = ignoreCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public void setFailedCount(int failedCount) {
            this.failedCount = failedCount;
        }

        public int getExistingFailedCount() {
            return existingFailedCount;
        }

        public void setExistingFailedCount(int existingFailedCount) {
            this.existingFailedCount = existingFailedCount;
        }

        public int getQuarantineCount() {
            return quarantineCount;
        }

        public void setQuarantineCount(int quarantineCount) {
            this.quarantineCount = quarantineCount;
        }

        public int getSuccessfulCount() {
            return successfulCount;
        }

        public void setSuccessfulCount(int successfulCount) {
            this.successfulCount = successfulCount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public void setSkippedCount(int skippedCount) {
            this.skippedCount = skippedCount;
        }

        public int getFixedCount() {
            return fixedCount;
        }

        public void setFixedCount(int fixedCount) {
            this.fixedCount = fixedCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getNewFailedCount() {
            return newFailedCount;
        }

        public void setNewFailedCount(int newFailedCount) {
            this.newFailedCount = newFailedCount;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class BambooVCSDTO {

        private String id;

        private String repositoryName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class BambooJobDTO {

        private int id;

        private List<BambooTestJobDTO> failedTests;

        private List<BambooTestJobDTO> successfulTests;

        public List<BambooTestJobDTO> getSuccessfulTests() {
            return successfulTests;
        }

        public void setSuccessfulTests(List<BambooTestJobDTO> successfulTests) {
            this.successfulTests = successfulTests;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<BambooTestJobDTO> getFailedTests() {
            return failedTests;
        }

        public void setFailedTests(List<BambooTestJobDTO> failedTests) {
            this.failedTests = failedTests;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class BambooTestJobDTO {

        private String name;

        private String methodName;

        private String className;

        private List<String> errors;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }
}
