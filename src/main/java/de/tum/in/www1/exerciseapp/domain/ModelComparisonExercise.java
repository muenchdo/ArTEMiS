package de.tum.in.www1.exerciseapp.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * A ModelComparisonExercise.
 */
@Entity
@DiscriminatorValue(value = "MCE")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ModelComparisonExercise extends ModelingExercise implements RepositoryAndContinuousIntegrationBasedExercise, Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "base_repository_url")
    private String baseRepositoryUrl;

    @Column(name = "base_build_plan_id")
    private String baseBuildPlanId;

    public ModelComparisonExercise baseRepositoryUrl(String baseRepositoryUrl) {
        this.baseRepositoryUrl = baseRepositoryUrl;
        return this;
    }

    public String getBaseRepositoryUrl() {
        return baseRepositoryUrl;
    }

    public void setBaseRepositoryUrl(String baseRepositoryUrl) {
        this.baseRepositoryUrl = baseRepositoryUrl;
    }

    @Override
    public String getBaseBuildPlanId() {
        return baseBuildPlanId;
    }

    public ModelComparisonExercise baseBuildPlanId(String baseBuildPlanId) {
        this.baseBuildPlanId = baseBuildPlanId;
        return this;
    }

    public void setBaseBuildPlanId(String baseBuildPlanId) {
        this.baseBuildPlanId = baseBuildPlanId;
    }

    @Override
    public URL getBaseRepositoryUrlAsUrl() {
        try {
            return new URL(baseRepositoryUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelComparisonExercise)) return false;
        if (!super.equals(o)) return false;
        ModelComparisonExercise that = (ModelComparisonExercise) o;
        return Objects.equals(baseRepositoryUrl, that.baseRepositoryUrl) &&
            Objects.equals(baseBuildPlanId, that.baseBuildPlanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baseRepositoryUrl, baseBuildPlanId);
    }

    @Override
    public String toString() {
        return "ModelComparisonExercise{" +
            "id=" + getId() +
            ", title='" + getTitle() + '\'' +
            ", baseRepositoryUrl='" + baseRepositoryUrl + '\'' +
            ", baseBuildPlanId='" + baseBuildPlanId + '\'' +
            '}';
    }
}
