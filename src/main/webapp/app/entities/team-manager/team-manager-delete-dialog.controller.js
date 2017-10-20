(function() {
    'use strict';

    angular
        .module('arTeMiSApp')
        .controller('TeamManagerDeleteController',TeamManagerDeleteController);

    TeamManagerDeleteController.$inject = ['$uibModalInstance', 'entity', 'TeamManager'];

    function TeamManagerDeleteController($uibModalInstance, entity, TeamManager) {
        var vm = this;

        vm.teamManager = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TeamManager.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
