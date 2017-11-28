(function() {
    'use strict';

    angular
        .module('artemisApp')
        .controller('ModelComparisonExerciseController', ModelComparisonExerciseController);

    ModelComparisonExerciseController.$inject = ['ModelComparisonExercise'];

    function ModelComparisonExerciseController(ModelComparisonExercise) {

        var vm = this;

        vm.modelComparisonExercises = [];

        loadAll();

        function loadAll() {
            ModelComparisonExercise.query(function(result) {
                vm.modelComparisonExercises = result;
                vm.searchQuery = null;
            });
        }
    }
})();
