/**
 * Created by muenchdo on 11/06/16.
 */
(function () {
    'use strict';

    angular
        .module('exerciseApplicationApp')
        .component('umlresult', {
            bindings: {
                participation: '<',
                loadDetails: '<',
                showBuildDate:'<',
                onNewResult: '&',
            },
            templateUrl: 'app/uml-editor/results/uml-result.html',
            controller: UmlResultController
        });

    UmlResultController.$inject = ['$http', '$uibModal', 'ParticipationResult', 'Repository', '$interval', '$scope', '$sce', 'JhiWebsocketService', 'Result'];

    function UmlResultController($http, $uibModal, ParticipationResult, Repository, $interval, $scope, $sce, JhiWebsocketService, Result) {
        var vm = this;
        vm.result = null;
        vm.$onInit = init;
        vm.hasResults = hasResults;
        vm.showDetails = showDetails;
        vm.textStyle = textStyle;

        function init() {
            refresh();


            var websocketChannel = 'topic/participation/' + vm.participation.id + '/umlexercise/assessmentResults';

            JhiWebsocketService.subscribe(websocketChannel);

            JhiWebsocketService.receive(websocketChannel).then(null, null, function (data) {
                // we get the uml result same data from websocket (pushed by server),
                // so we don't have to ask the server for result by making a http request
                vm.result = data;

                // Notify observer
                if (vm.onNewResult) {
                    console.log("Starting notifing new result");
                    vm.onNewResult({
                        $event: {
                            newResult: data
                        }
                    });
                }
            });

            $scope.$on('$destroy', function () {
                JhiWebsocketService.unsubscribe(websocketChannel);
            })

        }

        function refresh() {
            $http.get('api/participations/' + vm.participation.id + '/status', {
                ignoreLoadingBar: true
            }).then(function (response) {
                vm.queued = response.data === 'QUEUED';
                vm.building = response.data === 'BUILDING';
            }).finally(function () {
                if (!vm.queued && !vm.building) {

                    var notifyObserver = function (result) {
                        if (vm.onNewResult) {
                            vm.onNewResult({
                                $event: {
                                    newResult: result
                                }
                            });
                        }
                    };

                    if (vm.loadDetails === false) {
                        // Don't load the details (UmlAssessmentResult), only load it in the details dialog
                        vm.result = Result.umlExerciseResult({
                            id: vm.participation.id
                        }, notifyObserver);
                    } else {
                        // Also load the uml assessment result details (might be more expensive)
                        vm.result = Result.umlExerciseResultWithAssessmentDetails({
                            id: vm.participation.id
                        }, notifyObserver);
                    }
                }
            });
        }

        function textStyle() {
            if (vm.result === null)
                return "";

            if (vm.result.buildSuccessful === false){
                return "text-danger";
            }

            if (vm.result.parityWithSampleSolution === "100%")
                return "text-success";
             else
                 return "text-danger";
        }

        function hasResults() {
            return vm.result !== null
        }

        function showDetails(result) {
            $uibModal.open({
                size: 'lg',
                templateUrl: 'app/uml-editor/results/uml-result-detail.html',
                controller: ['$http', 'result', function ($http, result) {
                    var vm = this;

                    vm.$onInit = init;

                    function init() {
                        vm.loading = true;
                        Result.details({
                            id: result.id
                        }, function (details) {
                            vm.details = details;
                            if (details.length == 0) {
                                Repository.buildlogs({
                                    participationId: result.participation.id
                                }, function (buildLogs) {
                                    _.forEach(buildLogs, function (buildLog) {
                                        buildLog.log = $sce.trustAsHtml(buildLog.log);
                                    });
                                    vm.buildLogs = buildLogs;
                                    vm.loading = false;
                                });
                            } else {
                                vm.loading = false;
                            }
                        }, function (error) {
                            console.log("An error has occurred ");
                            console.log(error);
                        });
                    }
                }],
                resolve: {
                    result: result
                },
                controllerAs: '$ctrl'
            });
        }
    }
})();
