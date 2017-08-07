(function() {
    'use strict';

    angular
        .module('artemisApp')
        .controller('ParticipationDetailController', ParticipationDetailController);

    ParticipationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'Participation', 'User', 'Result', 'Exercise'];

    function ParticipationDetailController($scope, $rootScope, $stateParams, entity, Participation, User, Result, Exercise) {
        var vm = this;

        vm.participation = entity;

        var unsubscribe = $rootScope.$on('artemisApp:participationUpdate', function(event, result) {
            vm.participation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
