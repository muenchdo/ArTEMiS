package de.tum.in.www1.artemis.domain.modeling;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

import de.tum.in.www1.artemis.domain.Submission;

/**
 * A ModelingSubmission.
 */
@Entity
@DiscriminatorValue(value = "M")
public class ModelingSubmission extends Submission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "model")
    @Lob
    private String model;

    @Column(name = "explanation_text")
    @Lob
    private String explanationText;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    public String getModel() {
        return model;
    }

    public ModelingSubmission model(String model) {
        this.model = model;
        return this;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getExplanationText() {
        return explanationText;
    }

    public ModelingSubmission explanationText(String explanationText) {
        this.explanationText = explanationText;
        return this;
    }

    public void setExplanationText(String explanationText) {
        this.explanationText = explanationText;
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
        ModelingSubmission modelingSubmission = (ModelingSubmission) o;
        if (modelingSubmission.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), modelingSubmission.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ModelingSubmission{" + "id=" + getId() + "}";
    }
}
