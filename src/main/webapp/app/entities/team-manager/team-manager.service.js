(function() {
    'use strict';
    angular
        .module('arTeMiSApp')
        .factory('TeamManager', TeamManager);

    TeamManager.$inject = ['$resource'];

    function TeamManager ($resource) {
        var resourceUrl =  'api/team-managers/:id';

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
