package de.tum.in.www1.artemis.service.compass.umlmodel.classdiagram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.tum.in.www1.artemis.service.compass.umlmodel.UMLElement;

class UMLClassDiagramTest {

    private UMLClassDiagram classDiagram;

    @Mock
    UMLClass umlClass1;

    @Mock
    UMLClass umlClass2;

    @Mock
    UMLClass umlClass3;

    @Mock
    UMLRelationship umlRelationship1;

    @Mock
    UMLRelationship umlRelationship2;

    @Mock
    UMLRelationship umlRelationship3;

    @Mock
    UMLPackage umlPackage1;

    @Mock
    UMLPackage umlPackage2;

    @Mock
    UMLPackage umlPackage3;

    @Mock
    UMLAttribute umlAttribute;

    @Mock
    UMLMethod umlMethod;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        classDiagram = new UMLClassDiagram(123456789, List.of(umlClass1, umlClass2, umlClass3), List.of(umlRelationship1, umlRelationship2, umlRelationship3),
                List.of(umlPackage1, umlPackage2, umlPackage3));

        when(umlClass1.getElementByJSONID("class1")).thenReturn(umlClass1);
        when(umlClass2.getElementByJSONID("class2")).thenReturn(umlClass2);
        when(umlClass3.getElementByJSONID("class3")).thenReturn(umlClass3);
        when(umlClass1.getElementByJSONID("attribute")).thenReturn(umlAttribute);
        when(umlClass3.getElementByJSONID("method")).thenReturn(umlMethod);
        when(umlRelationship1.getJSONElementID()).thenReturn("relationship1");
        when(umlRelationship2.getJSONElementID()).thenReturn("relationship2");
        when(umlRelationship3.getJSONElementID()).thenReturn("relationship3");
        when(umlPackage1.getJSONElementID()).thenReturn("package1");
        when(umlPackage2.getJSONElementID()).thenReturn("package2");
        when(umlPackage3.getJSONElementID()).thenReturn("package3");
    }

    @Test
    void getElementByJSONID_null() {
        UMLElement element = classDiagram.getElementByJSONID(null);

        assertThat(element).isNull();
    }

    @Test
    void getElementByJSONID_emptyString() {
        UMLElement element = classDiagram.getElementByJSONID("");

        assertThat(element).isNull();
    }

    @Test
    void getElementByJSONID_getClass() {
        UMLElement element = classDiagram.getElementByJSONID("class1");

        assertThat(element).isEqualTo(umlClass1);
    }

    @Test
    void getElementByJSONID_getAttribute() {
        UMLElement element = classDiagram.getElementByJSONID("attribute");

        assertThat(element).isEqualTo(umlAttribute);
    }

    @Test
    void getElementByJSONID_getMethod() {
        UMLElement element = classDiagram.getElementByJSONID("method");

        assertThat(element).isEqualTo(umlMethod);
    }

    @Test
    void getElementByJSONID_getRelationship() {
        UMLElement element = classDiagram.getElementByJSONID("relationship2");

        assertThat(element).isEqualTo(umlRelationship2);
    }

    @Test
    void getElementByJSONID_getPackage() {
        UMLElement element = classDiagram.getElementByJSONID("package3");

        assertThat(element).isEqualTo(umlPackage3);
    }

    @Test
    void getElementByJSONID_notExisting() {
        UMLElement element = classDiagram.getElementByJSONID("nonExistingElement");

        assertThat(element).isNull();
    }

    @Test
    void getModelElements() {
        List<UMLElement> elementList = classDiagram.getModelElements();

        assertThat(elementList).containsExactlyInAnyOrder(umlClass1, umlClass2, umlClass3, umlRelationship1, umlRelationship2, umlRelationship3, umlPackage1, umlPackage2,
                umlPackage3);
    }

    @Test
    void getModelElements_emptyElementLists() {
        classDiagram = new UMLClassDiagram(987654321, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        List<UMLElement> elementList = classDiagram.getModelElements();

        assertThat(elementList).isEmpty();
    }
}
