(function() {
    'use strict';

    angular
        .module('arTeMiSApp')
        .controller('TeamManagerDialogController', TeamManagerDialogController);

    TeamManagerDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TeamManager'];

    function TeamManagerDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, TeamManager) {
        var vm = this;

        vm.teamManager = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.teamManager.id !== null) {
                TeamManager.update(vm.teamManager, onSaveSuccess, onSaveError);
            } else {
                TeamManager.save(vm.teamManager, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('arTeMiSApp:teamManagerUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
