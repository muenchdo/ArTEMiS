import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { SERVER_API_URL } from 'app/app.constants';
import { JhiWebsocketService } from 'app/core/websocket/websocket.service';
import { ProgrammingExerciseTestCase } from 'app/entities/programming-exercise-test-case.model';

export type ProgrammingExerciseTestCaseUpdate = { id: number; weight: number; afterDueDate: boolean };

export interface IProgrammingExerciseTestCaseService {
    subscribeForTestCases(exerciseId: number): Observable<ProgrammingExerciseTestCase[] | null>;
    notifyTestCases(exerciseId: number, testCases: ProgrammingExerciseTestCase[]): void;
    updateTestCase(exerciseId: number, testCaseUpdates: ProgrammingExerciseTestCaseUpdate[]): Observable<ProgrammingExerciseTestCase[]>;
    resetWeights(exerciseId: number): Observable<ProgrammingExerciseTestCase[]>;
}

@Injectable({ providedIn: 'root' })
export class ProgrammingExerciseTestCaseService implements IProgrammingExerciseTestCaseService, OnDestroy {
    public testCaseUrl = `${SERVER_API_URL}api/programming-exercise`;

    private connections: { [exerciseId: string]: string } = {};
    private subjects: { [exerciseId: string]: BehaviorSubject<ProgrammingExerciseTestCase[] | null> } = {};

    constructor(private jhiWebsocketService: JhiWebsocketService, private http: HttpClient) {}

    /**
     * On destroy unsubscribe all connections.
     */
    ngOnDestroy(): void {
        Object.values(this.connections).forEach((connection) => this.jhiWebsocketService.unsubscribe(connection));
    }

    /**
     * Subscribe to test case changes on the server.
     * Executes a REST request initially to get the current value, so that ideally no null value is emitted to the subscriber.
     *
     * If the result is an empty array, this will be translated into a null value.
     * This is done on purpose most likely this is an error as most programming exercises have at least one test case.
     *
     * @param exerciseId
     */
    subscribeForTestCases(exerciseId: number): Observable<ProgrammingExerciseTestCase[] | null> {
        if (this.subjects[exerciseId]) {
            return this.subjects[exerciseId] as Observable<ProgrammingExerciseTestCase[] | null>;
        } else {
            return this.getTestCases(exerciseId).pipe(
                map((testCases) => (testCases.length ? testCases : null)),
                catchError(() => of(null)),
                switchMap((testCases: ProgrammingExerciseTestCase[] | null) => this.initTestCaseSubscription(exerciseId, testCases)),
            );
        }
    }

    /**
     * Send new values for the test cases of an exercise to all subscribers.
     * @param exerciseId
     * @param testCases
     */
    public notifyTestCases(exerciseId: number, testCases: ProgrammingExerciseTestCase[]): void {
        if (this.subjects[exerciseId]) {
            this.subjects[exerciseId].next(testCases);
        }
    }

    /**
     * Executes a REST request to the test case endpoint.
     * @param exerciseId
     */
    private getTestCases(exerciseId: number): Observable<ProgrammingExerciseTestCase[]> {
        return this.http.get<ProgrammingExerciseTestCase[]>(`${this.testCaseUrl}/${exerciseId}/test-cases`);
    }

    /**
     * Update the weights with the provided values of the test cases.
     * Needs the exercise to verify permissions on the server.
     *
     * @param exerciseId
     * @param updates dto for updating test cases to avoid setting automatic parameters (e.g. active or testName)
     */
    public updateTestCase(exerciseId: number, updates: ProgrammingExerciseTestCaseUpdate[]): Observable<ProgrammingExerciseTestCase[]> {
        return this.http.patch<ProgrammingExerciseTestCase[]>(`${this.testCaseUrl}/${exerciseId}/update-test-cases`, updates);
    }

    /**
     * Use with care: Set all test case weights to 1.
     *
     * @param exerciseId
     */
    public resetWeights(exerciseId: number): Observable<ProgrammingExerciseTestCase[]> {
        return this.http.patch<ProgrammingExerciseTestCase[]>(`${this.testCaseUrl}/${exerciseId}/test-cases/reset-weights`, {});
    }

    /**
     * Set up the infrastructure for handling and reusing a new test case subscription.
     * @param exerciseId
     * @param initialValue
     */
    private initTestCaseSubscription(exerciseId: number, initialValue: ProgrammingExerciseTestCase[] | null) {
        const testCaseTopic = `/topic/programming-exercise/${exerciseId}/test-cases`;
        this.jhiWebsocketService.subscribe(testCaseTopic);
        this.connections[exerciseId] = testCaseTopic;
        this.subjects[exerciseId] = new BehaviorSubject(initialValue);
        this.jhiWebsocketService
            .receive(testCaseTopic)
            .pipe(
                map((testCases) => (testCases.length ? testCases : null)),
                tap((testCases) => this.notifySubscribers(exerciseId, testCases)),
            )
            .subscribe();
        return this.subjects[exerciseId];
    }

    /**
     * Notify the subscribers of the exercise specific test cases.
     * @param exerciseId
     * @param testCases
     */
    private notifySubscribers(exerciseId: number, testCases: ProgrammingExerciseTestCase[] | null) {
        this.subjects[exerciseId].next(testCases);
    }
}
