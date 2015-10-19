angular.module('courseSelection', [])
    .controller('courseSelectionCtrl', function ($scope, $http, $sce, $location, apiService, selectedCoursesService, helperService) {

        $scope.educationInstitutionOptions = {}; // {'Helsinki University' : true, 'Oulu AMK' : true};
        $scope.typeOptions = {};
        $scope.levelOptions = ["Any"];

        var findOptionsRecursively = function (learningOpportunityArray, partOf) {
            angular.forEach(learningOpportunityArray, function (opportunityWrapper) {
                var opportunity = opportunityWrapper.learningOpportunitySpecification;

                if (opportunity.type)
                    $scope.typeOptions[opportunity.type] = true;

                if (opportunity.level) {
                    var indexOf = $scope.levelOptions.indexOf(opportunity.level)
                    if (indexOf < 0)
                        $scope.levelOptions.push(opportunity.level);
                }

                if (opportunity.hasPart)
                    findOptionsRecursively(opportunity.hasPart, opportunity)
            });
            return;
        };

        var collectDataFromReports = function(reports){
            angular.forEach(reports, function (report) {
                $scope.learner = report.learner;

                var issuerTitle = helperService.getRightLanguage(report.issuer.title);
                $scope.educationInstitutionOptions[issuerTitle] = true;

                findOptionsRecursively(report.learningOpportunitySpecification);
            });
        };

        if (!selectedCoursesService.reports)
            apiService.getElmoAll().then(function (reports) {
                collectDataFromReports(reports);
                $scope.reports = reports;
                selectedCoursesService.reports = reports;
            })
        else {
            collectDataFromReports(selectedCoursesService.reports)
            $scope.reports = selectedCoursesService.reports;
        }

        apiService.getAbortHtml().then(function (html) {
            $scope.abort = html;
        });

        $scope.issuerFilter = function (report) {
            var title = helperService.getRightLanguage(report.issuer.title);
            return $scope.educationInstitutionOptions[title];
        };

        $scope.sendIds = function () {
            $location.path('preview');
        };
    });
