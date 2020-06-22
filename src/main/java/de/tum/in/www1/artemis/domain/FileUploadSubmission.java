package de.tum.in.www1.artemis.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.*;

import org.apache.commons.lang3.math.NumberUtils;

import de.tum.in.www1.artemis.config.Constants;
import de.tum.in.www1.artemis.exception.FilePathParsingException;
import de.tum.in.www1.artemis.service.FileService;

/**
 * A FileUploadSubmission.
 */
@Entity
@DiscriminatorValue(value = "F")
public class FileUploadSubmission extends Submission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    private FileService fileService = new FileService();

    @Column(name = "file_path")
    private String filePath;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    /**
     * Deletes solution file for this submission
     */
    @PostRemove
    public void onDelete() {
        if (filePath != null) {
            // delete old file if necessary
            final var splittedPath = filePath.split("/");
            final var shouldBeExerciseId = splittedPath.length >= 5 ? splittedPath[4] : null;
            if (!NumberUtils.isCreatable(shouldBeExerciseId)) {
                throw new FilePathParsingException("Unexpected String in upload file path. Should contain the exercise ID: " + shouldBeExerciseId);
            }
            final var exerciseId = Long.parseLong(shouldBeExerciseId);
            fileService.manageFilesForUpdatedFilePath(filePath, null, FileUploadSubmission.buildFilePath(exerciseId, getId()), getId(), true);
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public FileUploadSubmission filePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    /**
     * Builds file path for file upload submission.
     * @param exerciseId the id of the exercise
     * @param submissionId the id of the submission
     * @return path where submission for file upload exercise is stored
     */
    public static String buildFilePath(Long exerciseId, Long submissionId) {
        return Constants.FILE_UPLOAD_EXERCISES_FILEPATH + exerciseId + File.separator + submissionId + File.separator;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileUploadSubmission fileUploadSubmission = (FileUploadSubmission) o;
        if (fileUploadSubmission.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), fileUploadSubmission.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "FileUploadSubmission{" + "id=" + getId() + ", filePath='" + getFilePath() + "'" + "}";
    }
}
