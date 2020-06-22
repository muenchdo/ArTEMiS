import { Exercise, ExerciseType } from 'app/entities/exercise.model';
import { Course } from 'app/entities/course.model';
import { AssessmentType } from 'app/entities/assessment-type.model';

/**
 * The DiagramType enumeration. This has to be exactly the same as defined in Apollon (see diagram-type.d.ts)
 */
export enum UMLDiagramType {
    ClassDiagram = 'ClassDiagram',
    ObjectDiagram = 'ObjectDiagram',
    ActivityDiagram = 'ActivityDiagram',
    UseCaseDiagram = 'UseCaseDiagram',
    CommunicationDiagram = 'CommunicationDiagram',
    ComponentDiagram = 'ComponentDiagram',
    DeploymentDiagram = 'DeploymentDiagram',
}

export class ModelingExercise extends Exercise {
    public diagramType: UMLDiagramType;
    public sampleSolutionModel: string;
    public sampleSolutionExplanation: string;

    constructor(diagramType: UMLDiagramType, course?: Course) {
        super(ExerciseType.MODELING);
        this.course = course || null;
        this.diagramType = diagramType;
        if (this.diagramType === UMLDiagramType.ClassDiagram || this.diagramType === UMLDiagramType.ActivityDiagram) {
            this.assessmentType = AssessmentType.SEMI_AUTOMATIC;
        }
    }
}

export { UMLDiagramType as DiagramType };
