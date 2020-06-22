package de.tum.in.www1.artemis.service.compass.umlmodel.activitydiagram;

import java.util.Objects;

import com.google.common.base.CaseFormat;

import de.tum.in.www1.artemis.service.compass.strategy.NameSimilarity;
import de.tum.in.www1.artemis.service.compass.umlmodel.Similarity;
import de.tum.in.www1.artemis.service.compass.umlmodel.UMLElement;

public class UMLActivityNode extends UMLActivityElement {

    public enum UMLActivityNodeType {
        ACTIVITY_INITIAL_NODE, ACTIVITY_FINAL_NODE, ACTIVITY_ACTION_NODE, ACTIVITY_OBJECT_NODE, ACTIVITY_FORK_NODE, ACTIVITY_JOIN_NODE, ACTIVITY_DECISION_NODE, ACTIVITY_MERGE_NODE
    }

    private UMLActivityNodeType type;

    public UMLActivityNode(String name, String jsonElementID, UMLActivityNodeType type) {
        super(name, jsonElementID);

        this.type = type;
    }

    @Override
    public double similarity(Similarity<UMLElement> reference) {
        if (!(reference instanceof UMLActivityNode)) {
            return 0;
        }

        UMLActivityNode referenceNode = (UMLActivityNode) reference;

        if (!Objects.equals(getType(), referenceNode.getType())) {
            return 0;
        }

        return NameSimilarity.levenshteinSimilarity(name, referenceNode.getName());
    }

    @Override
    public String getType() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, type.name());
    }
}
