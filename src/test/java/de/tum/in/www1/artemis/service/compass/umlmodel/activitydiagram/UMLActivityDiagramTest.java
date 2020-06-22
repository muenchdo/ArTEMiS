package de.tum.in.www1.artemis.service.compass.umlmodel.activitydiagram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.tum.in.www1.artemis.service.compass.umlmodel.UMLElement;

class UMLActivityDiagramTest {

    private UMLActivityDiagram activityDiagram;

    @Mock
    UMLActivityNode umlActivityNode1;

    @Mock
    UMLActivityNode umlActivityNode2;

    @Mock
    UMLActivityNode umlActivityNode3;

    @Mock
    UMLActivity umlActivity1;

    @Mock
    UMLActivity umlActivity2;

    @Mock
    UMLActivity umlActivity3;

    @Mock
    UMLControlFlow umlControlFlow1;

    @Mock
    UMLControlFlow umlControlFlow2;

    @Mock
    UMLControlFlow umlControlFlow3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        activityDiagram = new UMLActivityDiagram(123456789, List.of(umlActivityNode1, umlActivityNode2, umlActivityNode3), List.of(umlActivity1, umlActivity2, umlActivity3),
                List.of(umlControlFlow1, umlControlFlow2, umlControlFlow3));

        when(umlActivityNode1.getJSONElementID()).thenReturn("activityNode1");
        when(umlActivityNode2.getJSONElementID()).thenReturn("activityNode2");
        when(umlActivityNode3.getJSONElementID()).thenReturn("activityNode3");
        when(umlActivity1.getJSONElementID()).thenReturn("activity1");
        when(umlActivity2.getJSONElementID()).thenReturn("activity2");
        when(umlActivity3.getJSONElementID()).thenReturn("activity3");
        when(umlControlFlow1.getJSONElementID()).thenReturn("controlFlow1");
        when(umlControlFlow2.getJSONElementID()).thenReturn("controlFlow2");
        when(umlControlFlow3.getJSONElementID()).thenReturn("controlFlow3");
    }

    @Test
    void getElementByJSONID_null() {
        UMLElement element = activityDiagram.getElementByJSONID(null);

        assertThat(element).isNull();
    }

    @Test
    void getElementByJSONID_emptyString() {
        UMLElement element = activityDiagram.getElementByJSONID("");

        assertThat(element).isNull();
    }

    @Test
    void getElementByJSONID_getActivityNode() {
        UMLElement element = activityDiagram.getElementByJSONID("activityNode2");

        assertThat(element).isEqualTo(umlActivityNode2);
    }

    @Test
    void getElementByJSONID_getActivity() {
        UMLElement element = activityDiagram.getElementByJSONID("activity2");

        assertThat(element).isEqualTo(umlActivity2);
    }

    @Test
    void getElementByJSONID_getControlFlow() {
        UMLElement element = activityDiagram.getElementByJSONID("controlFlow2");

        assertThat(element).isEqualTo(umlControlFlow2);
    }

    @Test
    void getElementByJSONID_notExisting() {
        UMLElement element = activityDiagram.getElementByJSONID("nonExistingElement");

        assertThat(element).isNull();
    }

    @Test
    void getModelElements() {
        List<UMLElement> elementList = activityDiagram.getModelElements();

        assertThat(elementList).containsExactlyInAnyOrder(umlActivityNode1, umlActivityNode2, umlActivityNode3, umlActivity1, umlActivity2, umlActivity3, umlControlFlow1,
                umlControlFlow2, umlControlFlow3);
    }

    @Test
    void getModelElements_emptyElementLists() {
        activityDiagram = new UMLActivityDiagram(987654321, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        List<UMLElement> elementList = activityDiagram.getModelElements();

        assertThat(elementList).isEmpty();
    }
}
