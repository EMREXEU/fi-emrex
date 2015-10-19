angular.module('courseSelection')
    .controller('courseSelectionPreviewCtrl', function ($scope, $sce, $http, selectedCoursesService, apiService, helperService) {
        apiService.getSubmitHtml(selectedCoursesService.selectedCourseIds).then(function (html) {
            $scope.review = html;
        });

        apiService.getAbortHtml().then(function (html) {
            $scope.abort = html;
        });

        $scope.numberOfCourses = 0;

        apiService.getElmoSelected(selectedCoursesService.selectedCourseIds).then(function (reports) {

            var reports = helperService.calculateAndFilter(reports);
            angular.forEach(reports, function(report){
                $scope.numberOfCourses += report.numberOfCourses;
            });
            $scope.reports = reports;
        });

        // there are learning opportunitites in report
        $scope.courseNumberFilter = function(report) {
            return (report.learningOpportunitySpecification);
        };

    })
;
