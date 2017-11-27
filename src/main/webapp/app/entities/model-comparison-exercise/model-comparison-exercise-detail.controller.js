(function() {
    'use strict';

    angular
        .module('arTeMiSApp')
        .controller('ModelComparisonExerciseDetailController', ModelComparisonExerciseDetailController);

    ModelComparisonExerciseDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ModelComparisonExercise'];

    function ModelComparisonExerciseDetailController($scope, $rootScope, $stateParams, previousState, entity, ModelComparisonExercise) {
        var vm = this;

        vm.modelComparisonExercise = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('arTeMiSApp:modelComparisonExerciseUpdate', function(event, result) {
            vm.modelComparisonExercise = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
