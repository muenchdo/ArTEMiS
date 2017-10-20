(function() {
    'use strict';

    angular
        .module('arTeMiSApp')
        .controller('ExerciseDetailController', ExerciseDetailController);

    ExerciseDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Exercise', 'Participation', 'TeamManager', 'Course'];

    function ExerciseDetailController($scope, $rootScope, $stateParams, previousState, entity, Exercise, Participation, TeamManager, Course) {
        var vm = this;

        vm.exercise = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('arTeMiSApp:exerciseUpdate', function(event, result) {
            vm.exercise = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
