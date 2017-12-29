(function() {
    'use strict';

    angular
        .module('artemisApp')
        .controller('ModelComparisonExerciseDeleteController',ModelComparisonExerciseDeleteController);

    ModelComparisonExerciseDeleteController.$inject = ['$uibModalInstance', 'entity', 'ModelComparisonExercise'];

    function ModelComparisonExerciseDeleteController($uibModalInstance, entity, ModelComparisonExercise) {
        var vm = this;

        vm.modelComparisonExercise = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ModelComparisonExercise.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
