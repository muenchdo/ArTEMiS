(function() {
    'use strict';
    angular
        .module('artemisApp')
        .factory('ModelComparisonExercise', ModelComparisonExercise);

    ModelComparisonExercise.$inject = ['$resource'];

    function ModelComparisonExercise ($resource) {
        var resourceUrl =  'api/model-comparison-exercises/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
