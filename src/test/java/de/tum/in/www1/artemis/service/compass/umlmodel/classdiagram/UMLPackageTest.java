package de.tum.in.www1.artemis.service.compass.umlmodel.classdiagram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import me.xdrop.fuzzywuzzy.FuzzySearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UMLPackageTest {

    private UMLPackage umlPackage;

    @Mock
    UMLPackage referencePackage;

    @Mock
    UMLClass class1;

    @Mock
    UMLClass class2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        umlPackage = new UMLPackage("myPackage", List.of(class1, class2), "packageId");
    }

    @Test
    void similarity_null() {
        double similarity = umlPackage.similarity(null);

        assertThat(similarity).isEqualTo(0);
    }

    @Test
    void similarity_differentElementType() {
        double similarity = umlPackage.similarity(mock(UMLClass.class));

        assertThat(similarity).isEqualTo(0);
    }

    @Test
    void similarity_samePackageName() {
        when(referencePackage.getName()).thenReturn("myPackage");

        double similarity = umlPackage.similarity(referencePackage);

        assertThat(similarity).isEqualTo(1);
    }

    @Test
    void similarity_differentPackageName() {
        when(referencePackage.getName()).thenReturn("differentPackageName");
        double expectedNameSimilarity = FuzzySearch.ratio("myPackage", "differentPackageName") / 100.0;

        double similarity = umlPackage.similarity(referencePackage);

        assertThat(similarity).isEqualTo(expectedNameSimilarity);
    }
}
