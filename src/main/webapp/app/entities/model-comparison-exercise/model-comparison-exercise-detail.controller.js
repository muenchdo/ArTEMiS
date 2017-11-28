(function() {
    'use strict';

    angular
        .module('artemisApp')
        .controller('ModelComparisonExerciseDetailController', ModelComparisonExerciseDetailController);

    ModelComparisonExerciseDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ModelComparisonExercise'];

    function ModelComparisonExerciseDetailController($scope, $rootScope, $stateParams, previousState, entity, ModelComparisonExercise) {
        var vm = this;

        vm.modelComparisonExercise = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('artemisApp:modelComparisonExerciseUpdate', function(event, result) {
            vm.modelComparisonExercise = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
