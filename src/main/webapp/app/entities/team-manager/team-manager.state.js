(function() {
    'use strict';

    angular
        .module('arTeMiSApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('team-manager', {
            parent: 'entity',
            url: '/team-manager',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'arTeMiSApp.teamManager.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/team-manager/team-managers.html',
                    controller: 'TeamManagerController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('teamManager');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('team-manager-detail', {
            parent: 'team-manager',
            url: '/team-manager/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'arTeMiSApp.teamManager.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/team-manager/team-manager-detail.html',
                    controller: 'TeamManagerDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('teamManager');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'TeamManager', function($stateParams, TeamManager) {
                    return TeamManager.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'team-manager',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('team-manager-detail.edit', {
            parent: 'team-manager-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/team-manager/team-manager-dialog.html',
                    controller: 'TeamManagerDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['TeamManager', function(TeamManager) {
                            return TeamManager.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('team-manager.new', {
            parent: 'team-manager',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/team-manager/team-manager-dialog.html',
                    controller: 'TeamManagerDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                teamName: null,
                                test: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('team-manager', null, { reload: 'team-manager' });
                }, function() {
                    $state.go('team-manager');
                });
            }]
        })
        .state('team-manager.edit', {
            parent: 'team-manager',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/team-manager/team-manager-dialog.html',
                    controller: 'TeamManagerDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['TeamManager', function(TeamManager) {
                            return TeamManager.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('team-manager', null, { reload: 'team-manager' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('team-manager.delete', {
            parent: 'team-manager',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/team-manager/team-manager-delete-dialog.html',
                    controller: 'TeamManagerDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['TeamManager', function(TeamManager) {
                            return TeamManager.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('team-manager', null, { reload: 'team-manager' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
