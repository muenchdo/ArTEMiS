(function () {
    'use strict';

    angular
        .module('artemisApp')
        .controller('ModelComparisonExerciseDialogController', ModelComparisonExerciseDialogController);

    ModelComparisonExerciseDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ModelComparisonExercise', 'Course'];

    function ModelComparisonExerciseDialogController($timeout, $scope, $stateParams, $uibModalInstance, entity, ModelComparisonExercise, Course) {
        var vm = this;

        vm.modelComparisonExercise = entity;

        if (vm.modelComparisonExercise.releaseDate) {
            vm.modelComparisonExercise.releaseDate = new Date(vm.modelComparisonExercise.releaseDate);
        }
        if (vm.modelComparisonExercise.dueDate) {
            vm.modelComparisonExercise.dueDate = new Date(vm.modelComparisonExercise.dueDate);
        }

        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.courses = Course.query();

        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function save() {
            vm.isSaving = true;
            if (vm.modelComparisonExercise.id !== null) {
                ModelComparisonExercise.update(vm.modelComparisonExercise, onSaveSuccess, onSaveError);
            } else {
                ModelComparisonExercise.save(vm.modelComparisonExercise, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('artemisApp:modelComparisonExerciseUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.releaseDate = false;
        vm.datePickerOpenStatus.dueDate = false;

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }

    }
})();
