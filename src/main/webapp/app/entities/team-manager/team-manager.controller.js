(function() {
    'use strict';

    angular
        .module('arTeMiSApp')
        .controller('TeamManagerController', TeamManagerController);

    TeamManagerController.$inject = ['TeamManager'];

    function TeamManagerController(TeamManager) {

        var vm = this;

        vm.teamManagers = [];

        loadAll();

        function loadAll() {
            TeamManager.query(function(result) {
                vm.teamManagers = result;
                vm.searchQuery = null;
            });
        }
    }
})();
