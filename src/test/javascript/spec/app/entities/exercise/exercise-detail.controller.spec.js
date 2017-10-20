'use strict';

describe('Controller Tests', function() {

    describe('Exercise Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockExercise, MockParticipation, MockTeamManager, MockCourse;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockExercise = jasmine.createSpy('MockExercise');
            MockParticipation = jasmine.createSpy('MockParticipation');
            MockTeamManager = jasmine.createSpy('MockTeamManager');
            MockCourse = jasmine.createSpy('MockCourse');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'Exercise': MockExercise,
                'Participation': MockParticipation,
                'TeamManager': MockTeamManager,
                'Course': MockCourse
            };
            createController = function() {
                $injector.get('$controller')("ExerciseDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'arTeMiSApp:exerciseUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
