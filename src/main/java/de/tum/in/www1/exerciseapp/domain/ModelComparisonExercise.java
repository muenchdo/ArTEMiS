package de.tum.in.www1.exerciseapp.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A ModelComparisonExercise.
 */
@Entity
@Table(name = "model_comparison_exercise")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ModelComparisonExercise implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_repository_url")
    private String baseRepositoryUrl;

    @Column(name = "base_build_plan_id")
    private String baseBuildPlanId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBaseRepositoryUrl() {
        return baseRepositoryUrl;
    }

    public ModelComparisonExercise baseRepositoryUrl(String baseRepositoryUrl) {
        this.baseRepositoryUrl = baseRepositoryUrl;
        return this;
    }

    public void setBaseRepositoryUrl(String baseRepositoryUrl) {
        this.baseRepositoryUrl = baseRepositoryUrl;
    }

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelComparisonExercise modelComparisonExercise = (ModelComparisonExercise) o;
        if (modelComparisonExercise.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), modelComparisonExercise.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ModelComparisonExercise{" +
            "id=" + getId() +
            ", baseRepositoryUrl='" + getBaseRepositoryUrl() + "'" +
            ", baseBuildPlanId='" + getBaseBuildPlanId() + "'" +
            "}";
    }
}
