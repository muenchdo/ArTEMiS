package de.tum.in.www1.artemis.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A TextCluster.
 */
@Entity
@Table(name = "text_cluster")
public class TextCluster implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "probabilities")
    private byte[] probabilities;

    @Lob
    @Column(name = "distance_matrix")
    private byte[] distanceMatrix;

    @OneToMany(mappedBy = "cluster")
    @OrderBy("position_in_cluster")
    @JsonIgnoreProperties("cluster")
    private List<TextBlock> blocks = new ArrayList<>();

    @ManyToOne
    @JsonIgnore
    private TextExercise exercise;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double[] getProbabilities() {
        return castFromBinary(probabilities);
    }

    public TextCluster probabilities(double[] probabilities) {
        setProbabilities(probabilities);
        return this;
    }

    public void setProbabilities(double[] probabilities) {
        this.probabilities = castToBinary(probabilities);
    }

    public double[][] getDistanceMatrix() {
        return castFromBinary(distanceMatrix);
    }

    public TextCluster distanceMatrix(double[][] distanceMatrix) {
        setDistanceMatrix(distanceMatrix);
        return this;
    }

    public void setDistanceMatrix(double[][] distanceMatrix) {
        this.distanceMatrix = castToBinary(distanceMatrix);
    }

    private int getBlockIndex(TextBlock textBlock) {
        return blocks.indexOf(textBlock);
    }

    public List<TextBlock> getBlocks() {
        return blocks;
    }

    public TextCluster blocks(List<TextBlock> textBlocks) {
        this.blocks = textBlocks;
        updatePositions();
        return this;
    }

    /**
     * Adds a TextBlock to the Cluster
     * @param textBlock the TextBlock which should be added
     * @return the Cluster Object with the new TextBlock
     */
    public TextCluster addBlocks(TextBlock textBlock) {
        int newPosition = this.blocks.size();
        this.blocks.add(textBlock);
        textBlock.setCluster(this);
        textBlock.setPositionInCluster(newPosition);
        return this;
    }

    public TextCluster removeBlocks(TextBlock textBlock) {
        this.blocks.remove(textBlock);
        textBlock.setCluster(null);
        textBlock.setPositionInCluster(null);
        return this;
    }

    public void setBlocks(List<TextBlock> textBlocks) {
        this.blocks = textBlocks;
        updatePositions();
    }

    public TextExercise getExercise() {
        return exercise;
    }

    public TextCluster exercise(TextExercise exercise) {
        setExercise(exercise);
        return this;
    }

    public void setExercise(TextExercise exercise) {
        this.exercise = exercise;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public int size() {
        return blocks.size();
    }

    /**
     * Calculates the distance between two textblocks if they are in the same cluster
     * @param first the first TextBlock
     * @param second the second Textblock
     * @return the distance between the two parameters
     */
    public double distanceBetweenBlocks(TextBlock first, TextBlock second) {
        int firstIndex = getBlockIndex(first);
        int secondIndex = getBlockIndex(second);

        if (firstIndex == -1 || secondIndex == -1) {
            throw new IllegalArgumentException("Cannot compute distance to Text Block outside cluster.");
        }

        return getDistanceMatrix()[firstIndex][secondIndex];
    }

    private void updatePositions() {
        for (int i = 0; i < size(); i++) {
            blocks.get(i).setPositionInCluster(i);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TextCluster)) {
            return false;
        }
        return id != null && id.equals(((TextCluster) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TextCluster{" + "id=" + getId() + (exercise != null ? ", exercise='" + exercise.getId() + "'" : "") + ", size='" + size() + "'" + "}";
    }

    // region Binary Cast
    @SuppressWarnings("unchecked")
    private <T> T castFromBinary(byte[] data) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (final ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private <T> byte[] castToBinary(T data) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(data);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public int openTextBlockCount() {
        return (int) blocks.stream().filter(textBlock -> !textBlock.isAssessable()).count();
    }
    // endregion
}
