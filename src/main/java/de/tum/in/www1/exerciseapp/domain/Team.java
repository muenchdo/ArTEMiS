package de.tum.in.www1.exerciseapp.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Team.
 */
@Entity
@Table(name = "exercise")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id")
    private String teamID;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "jhi_min")
    private Integer min;

    @Column(name = "jhi_max")
    private Integer max;

    @ManyToOne
    private Exercise exercise;

    @ManyToOne
    private TeamManager teamManager;

    // jhipster-needle-entity-add-field - Jhipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTeamID() {
        return teamID;
    }

    public Team teamID(String teamID) {
        this.teamID = teamID;
        return this;
    }

    public void setTeamID(String teamID) {
        this.teamID = teamID;
    }

    public String getTeamName() {
        return teamName;
    }

    public Team teamName(String teamName) {
        this.teamName = teamName;
        return this;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Integer getMin() {
        return min;
    }

    public Team min(Integer min) {
        this.min = min;
        return this;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public Team max(Integer max) {
        this.max = max;
        return this;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public Team exercise(Exercise exercise) {
        this.exercise = exercise;
        return this;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public Team teamManager(TeamManager teamManager) {
        this.teamManager = teamManager;
        return this;
    }

    public void setTeamManager(TeamManager teamManager) {
        this.teamManager = teamManager;
    }
    // jhipster-needle-entity-add-getters-setters - Jhipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Team team = (Team) o;
        if (team.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), team.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Team{" +
            "id=" + getId() +
            ", teamID='" + getTeamID() + "'" +
            ", teamName='" + getTeamName() + "'" +
            ", min='" + getMin() + "'" +
            ", max='" + getMax() + "'" +
            "}";
    }
}
