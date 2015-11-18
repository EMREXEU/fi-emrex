app = angular.module('review', ['ngRoute', 'ngCookies', 'selectedCourses', 'helper', 'learningReport']);

app.config(function ($routeProvider, $httpProvider) {

    $routeProvider.
        when('/', {
            templateUrl: 'partials/reviewResults.html',
            controller: 'home'
        }).
        otherwise({
            redirectTo: '/'
        });

    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
});

app.controller('home', function ($scope, $http, $window, $sce, helperService) {
    $scope.numberOfCourses = 0;
    $scope.resultsImported = false;

    $scope.import = function () {
        $http.get('api/store').success(function () {
            $scope.resultsImported = true;
        });
    }
    
    $scope.getTitle = function(learningReport) {
        return helperService.getRightLanguage(learningReport.issuer.title);
    }
    
    $scope.abort = function () {
        $window.location.href = '/smp/abort';
    }

    $http.get('api/questionnaire').success(function(response) {
        $scope.questionnaireUrl = $sce.trustAsUrl(response.link);
    });

    $http.post('api/reports').success(function (response) {
        var reports = [];
        angular.forEach(response, function (item) {
            var report = angular.fromJson(item.report).report;
            report.verification = item.verification;
            reports.push(report);
        });

        reports = helperService.fixReports(reports);

        $scope.reports = helperService.calculateAndFilter(reports);
        angular.forEach(reports, function (report) {
            $scope.numberOfCourses += report.numberOfCourses;
        });
    });

    $scope.verificationLimit = 100;

});



