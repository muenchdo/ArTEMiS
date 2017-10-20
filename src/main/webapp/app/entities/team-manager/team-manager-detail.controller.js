(function() {
    'use strict';

    angular
        .module('arTeMiSApp')
        .controller('TeamManagerDetailController', TeamManagerDetailController);

    TeamManagerDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TeamManager'];

    function TeamManagerDetailController($scope, $rootScope, $stateParams, previousState, entity, TeamManager) {
        var vm = this;

        vm.teamManager = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('arTeMiSApp:teamManagerUpdate', function(event, result) {
            vm.teamManager = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
