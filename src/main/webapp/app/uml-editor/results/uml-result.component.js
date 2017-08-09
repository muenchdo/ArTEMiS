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
                showBuildDate: '<',
                onNewResult: '&',
            },
            templateUrl: 'app/uml-editor/results/uml-result.html',
            controller: UmlResultController
        });

    UmlResultController.$inject = ['$http', '$uibModal', 'ParticipationResult', 'Repository', '$interval', '$scope', '$sce', 'JhiWebsocketService', 'Result'];

    function UmlResultController($http, $uibModal, ParticipationResult, Repository, $interval, $scope, $sce, JhiWebsocketService, Result) {
        var vm = this;
        vm.result = null;
        vm.loading = false;
        vm.$onInit = init;
        vm.hasResults = hasResults;
        vm.showDetails = showDetails;
        vm.textStyle = textStyle;

        var participationId = vm.participation.id

        function init() {
            refresh();


            var websocketChannel = '/topic/participation/' + vm.participation.id + '/newResults';

            JhiWebsocketService.subscribe(websocketChannel);

            JhiWebsocketService.receive(websocketChannel).then(null, null, function (data) {
                // we get the uml result same data from websocket (pushed by server),
                // so we don't have to ask the server for result by making a http request
                vm.result = data;
                console.log("uml-result: Received notification in");
                // Notify observer
                if (vm.onNewResult) {
                    console.log("Starting notification new result");
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
            vm.loading = true;
            $http.get('api/participations/' + vm.participation.id + '/status', {
                ignoreLoadingBar: true
            }).then(function (response) {
                vm.queued = response.data === 'QUEUED';
                vm.building = response.data === 'BUILDING';
            }).finally(function () {
                if (!vm.queued && !vm.building) {

                    var notifyObserver = function (result) {
                        vm.result = result;
                        vm.loading = false;
                        if (vm.onNewResult) {
                            vm.onNewResult({
                                $event: {
                                    newResult: result
                                }
                            });
                        }
                    };

                    var errorHandler = function (error) {
                        console.log("Error while loading results: ");
                        console.log(error);
                        vm.loading = false;
                    };

                    if (vm.loadDetails === false) {
                        // Don't load the details (UmlAssessmentResult), only load it in the details dialog
                        Result.umlExerciseResult({
                            id: vm.participation.id
                        }, notifyObserver, errorHandler);
                    } else {
                        // Also load the uml assessment result details (might be more expensive)
                        Result.umlExerciseResultWithAssessmentDetails({
                            id: vm.participation.id
                        }, notifyObserver, errorHandler);
                    }
                } else {
                    vm.loading = false;
                }
            });
        }

        function textStyle() {
            if (vm.result === null)
                return "";

            if (vm.result.buildSuccessful === false) {
                return "text-danger";
            }

            if (vm.result.parityWithSampleSolution === "100%")
                return "text-success";
            else
                return "text-danger";
        }

        function hasResults() {
            return vm.result !== null;
        }

        function showDetails(result) {
            $uibModal.open({
                size: 'lg',
                templateUrl: 'app/uml-editor/results/uml-result-detail.html',
                controller: ['$http', 'result', function ($http, result) {
                    var vm = this;

                    vm.$onInit = init;
                    vm.buildResult = result;
                    vm.buildLogs = null;
                    console.log("details");
                    console.log(vm.buildResult);

                    function init() {
                        result.then(function (buildResult) {

                            console.log("Result details");
                            console.log(buildResult);
                            console.log(buildResult.result);

                            if (buildResult.buildSuccessful === true) {
                                if (buildResult.result !== null) {
                                    // All details are already there, so nothing to load
                                    vm.loading = false;
                                    vm.buildResult = buildResult;
                                    console.log("Everything is loaded");
                                } else {
                                    // We have to load the assessment details

                                    console.log("Have to load the details");
                                    Result.umlExerciseResultWithAssessmentDetails({
                                        id: participationId
                                    }, function (buildResult) {
                                        vm.buildResult = buildResult;
                                        vm.loading = false;

                                        console.log("Details loaded");
                                    }, function (error) {
                                        vm.loading = false;
                                        console.log("An error has occured");
                                        console.log(error);
                                    });
                                }

                                vm.buildLogs = null;
                            } else {
                                // Build not successful, which means there was an error on continious integration

                                console.log("Build not successfull. Loading logs");
                                vm.loading = true;

                                Repository.buildlogs({
                                    participationId: participationId
                                }, function (buildLogs) {
                                    _.forEach(buildLogs, function (buildLog) {
                                        buildLog.log = $sce.trustAsHtml(buildLog.log);
                                    });
                                    vm.buildLogs = buildLogs;
                                    vm.loading = false;
                                    console.log("Logs loaded");
                                }, function (error) {
                                    vm.loading = false;
                                    vm.buildLogs = null;
                                    console.log("An error has occured");
                                    console.log(error);
                                });

                            }
                            /*
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
                            */
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
