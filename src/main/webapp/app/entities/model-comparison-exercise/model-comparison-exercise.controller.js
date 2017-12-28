(function() {
    'use strict';

    angular
        .module('artemisApp')
        .controller('ModelComparisonExerciseController', ModelComparisonExerciseController);

    ModelComparisonExerciseController.$inject = ['ModelComparisonExercise',  'courseEntity', "CourseModelComparisonExercises"];

    function ModelComparisonExerciseController(ModelComparisonExercise, courseEntity, CourseModelComparisonExercises) {

        var vm = this;

        vm.modelComparisonExercises = [];
        vm.sort = sort;
        vm.predicate = 'id';
        vm.reverse = true;
        vm.course = courseEntity;

        function load() {
            if (vm.course) {
                loadForCourse(vm.course);
            } else {
                loadAll();
            }
        }

        load();

        function loadAll() {
            ModelComparisonExercise.query(function(result) {
                vm.modelComparisonExercises = result;
                vm.searchQuery = null;
            });
        }


        function loadForCourse(course) {
            CourseModelComparisonExercises.query({
                courseId: course.id
            }, function (result) {
                vm.modelComparisonExercises = result;
                vm.searchQuery = null;
            });
        }

        function sort() {
            vm.modelComparisonExercises.sort(function (a, b) {
                var result = (a[vm.predicate] < b[vm.predicate]) ? -1 : (a[vm.predicate] > b[vm.predicate]) ? 1 : (
                    (a.id < b.id) ? -1 : (a.id > b.id) ? 1 : 0
                );
                return result * (vm.reverse ? -1 : 1);
            });
        }
    }
})();
