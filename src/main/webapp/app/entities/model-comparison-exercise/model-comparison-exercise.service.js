(function () {
    'use strict';
    angular
        .module('artemisApp')
        .factory('ModelComparisonExercise', ModelComparisonExercise);

    ModelComparisonExercise.$inject = ['$resource'];

    function ModelComparisonExercise($resource) {
        var resourceUrl = 'api/model-comparison-exercises/:id';

        return $resource(resourceUrl, {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'save': {
                method: 'POST',
                transformRequest: addType
            },
            'update': {
                method: 'PUT',
                transformRequest: addType
            }
        });
    }

    // Type property has to be added to the exercise so that Jackson can
    // deserialize the data into correct concrete implementation of Exercise class
    var addType = function (data) {
        data.type = "model-comparison-exercise";
        data = angular.toJson(data);
        return data;
    };
})();
