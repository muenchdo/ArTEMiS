(function() {
    'use strict';

    angular
        .module('arTeMiSApp')
        .controller('TeamDetailController', TeamDetailController);

    TeamDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Team', 'Exercise', 'TeamManager'];

    function TeamDetailController($scope, $rootScope, $stateParams, previousState, entity, Team, Exercise, TeamManager) {
        var vm = this;

        vm.team = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('arTeMiSApp:teamUpdate', function(event, result) {
            vm.team = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
