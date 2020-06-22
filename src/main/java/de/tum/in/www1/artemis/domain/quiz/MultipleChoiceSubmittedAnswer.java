package de.tum.in.www1.artemis.domain.quiz;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonView;

import de.tum.in.www1.artemis.domain.SubmittedAnswer;
import de.tum.in.www1.artemis.domain.view.QuizView;

/**
 * A MultipleChoiceSubmittedAnswer.
 */
@Entity
@DiscriminatorValue(value = "MC")
// @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonTypeName("multiple-choice")
public class MultipleChoiceSubmittedAnswer extends SubmittedAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "multiple_choice_submitted_answer_selected_options", joinColumns = @JoinColumn(name = "multiple_choice_submitted_answers_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "selected_options_id", referencedColumnName = "id"))
    @JsonView(QuizView.Before.class)
    private Set<AnswerOption> selectedOptions = new HashSet<>();

    // jhipster-needle-entity-add-field - Jhipster will add fields here, do not remove

    public Set<AnswerOption> getSelectedOptions() {
        return selectedOptions;
    }

    public MultipleChoiceSubmittedAnswer selectedOptions(Set<AnswerOption> answerOptions) {
        this.selectedOptions = answerOptions;
        return this;
    }

    public MultipleChoiceSubmittedAnswer addSelectedOptions(AnswerOption answerOption) {
        this.selectedOptions.add(answerOption);
        return this;
    }

    public MultipleChoiceSubmittedAnswer removeSelectedOptions(AnswerOption answerOption) {
        this.selectedOptions.remove(answerOption);
        return this;
    }

    public void setSelectedOptions(Set<AnswerOption> answerOptions) {
        this.selectedOptions = answerOptions;
    }
    // jhipster-needle-entity-add-getters-setters - Jhipster will add getters and setters here, do not remove

    /**
     * Check if the given answer option is selected in this submitted answer
     * 
     * @param answerOption the answer option to check for
     * @return true if the answer option is selected, false otherwise
     */
    public boolean isSelected(AnswerOption answerOption) {
        // search for this answer option in the selected answer options
        for (AnswerOption selectedOption : getSelectedOptions()) {
            if (selectedOption.getId().longValue() == answerOption.getId().longValue()) {
                // this answer option is selected => we can stop searching
                return true;
            }
        }
        // we didn't find the answer option => it wasn't selected
        return false;
    }

    /**
     * Check if answerOptions were deleted and delete reference to in selectedOptions
     * 
     * @param question the changed question with the answerOptions
     */
    private void checkAndDeleteSelectedOptions(MultipleChoiceQuestion question) {

        if (question != null) {
            // Check if an answerOption was deleted and delete reference to in selectedOptions
            Set<AnswerOption> selectedOptionsToDelete = new HashSet<>();
            for (AnswerOption answerOption : this.getSelectedOptions()) {
                if (!question.getAnswerOptions().contains(answerOption)) {
                    selectedOptionsToDelete.add(answerOption);
                }
            }
            this.getSelectedOptions().removeAll(selectedOptionsToDelete);
        }
    }

    /**
     * Delete all references to question and answers if the question was changed
     *
     * @param quizExercise the changed quizExercise-object
     */
    public void checkAndDeleteReferences(QuizExercise quizExercise) {

        if (!quizExercise.getQuizQuestions().contains(getQuizQuestion())) {
            setQuizQuestion(null);
            selectedOptions = null;
        }
        else {
            // find same quizQuestion in quizExercise
            QuizQuestion quizQuestion = quizExercise.findQuestionById(getQuizQuestion().getId());

            // Check if an answerOption was deleted and delete reference to in selectedOptions
            checkAndDeleteSelectedOptions((MultipleChoiceQuestion) quizQuestion);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultipleChoiceSubmittedAnswer multipleChoiceSubmittedAnswer = (MultipleChoiceSubmittedAnswer) o;
        if (multipleChoiceSubmittedAnswer.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), multipleChoiceSubmittedAnswer.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MultipleChoiceSubmittedAnswer{" + "id=" + getId() + "}";
    }
}
