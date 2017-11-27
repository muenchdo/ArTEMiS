(function() {
    'use strict';

    angular
        .module('arTeMiSApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('model-comparison-exercise', {
            parent: 'entity',
            url: '/model-comparison-exercise',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'arTeMiSApp.modelComparisonExercise.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/model-comparison-exercise/model-comparison-exercises.html',
                    controller: 'ModelComparisonExerciseController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('modelComparisonExercise');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('model-comparison-exercise-detail', {
            parent: 'model-comparison-exercise',
            url: '/model-comparison-exercise/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'arTeMiSApp.modelComparisonExercise.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/model-comparison-exercise/model-comparison-exercise-detail.html',
                    controller: 'ModelComparisonExerciseDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('modelComparisonExercise');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ModelComparisonExercise', function($stateParams, ModelComparisonExercise) {
                    return ModelComparisonExercise.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'model-comparison-exercise',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('model-comparison-exercise-detail.edit', {
            parent: 'model-comparison-exercise-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/model-comparison-exercise/model-comparison-exercise-dialog.html',
                    controller: 'ModelComparisonExerciseDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ModelComparisonExercise', function(ModelComparisonExercise) {
                            return ModelComparisonExercise.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('model-comparison-exercise.new', {
            parent: 'model-comparison-exercise',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/model-comparison-exercise/model-comparison-exercise-dialog.html',
                    controller: 'ModelComparisonExerciseDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                baseRepositoryUrl: null,
                                baseBuildPlanId: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('model-comparison-exercise', null, { reload: 'model-comparison-exercise' });
                }, function() {
                    $state.go('model-comparison-exercise');
                });
            }]
        })
        .state('model-comparison-exercise.edit', {
            parent: 'model-comparison-exercise',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/model-comparison-exercise/model-comparison-exercise-dialog.html',
                    controller: 'ModelComparisonExerciseDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ModelComparisonExercise', function(ModelComparisonExercise) {
                            return ModelComparisonExercise.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('model-comparison-exercise', null, { reload: 'model-comparison-exercise' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('model-comparison-exercise.delete', {
            parent: 'model-comparison-exercise',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/model-comparison-exercise/model-comparison-exercise-delete-dialog.html',
                    controller: 'ModelComparisonExerciseDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ModelComparisonExercise', function(ModelComparisonExercise) {
                            return ModelComparisonExercise.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('model-comparison-exercise', null, { reload: 'model-comparison-exercise' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
