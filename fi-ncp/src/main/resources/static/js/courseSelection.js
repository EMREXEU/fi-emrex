angular.module('courseSelection', [])
    .controller('courseSelectionCtrl', function ($scope, $http, $sce, $location, apiService, selectedCoursesService, helperService) {

        $scope.educationInstitutionOptions = {}; // {'Helsinki University' : true, 'Oulu AMK' : true};
        $scope.typeOptions = {};
        $scope.levelOptions = ["Any"];

        var findOptionsRecursively = function (innerFunction, learningOpportunityArray, partOf) {
            angular.forEach(learningOpportunityArray, function (opportunityWrapper) {
                var opportunity = opportunityWrapper.learningOpportunitySpecification;
                innerFunction(opportunity);


                if (opportunity.hasPart)
                    findOptionsRecursively(innerFunction, opportunity.hasPart, opportunity)
            });
            return;
        };

        var collectOptions = function(opportunity){
            if (opportunity.type)
                $scope.typeOptions[opportunity.type] = true;

            if (opportunity.specifies.learningOpportunityInstance.credit.level) {
                var indexOf = $scope.levelOptions.indexOf(opportunity.specifies.learningOpportunityInstance.credit.level)
                if (indexOf < 0)
                    $scope.levelOptions.push(opportunity.specifies.learningOpportunityInstance.credit.level);
            }
        }

        var collectDataFromReports = function(reports){
            angular.forEach(reports, function (report) {
                $scope.learner = report.learner;

                var issuerTitle = helperService.getRightLanguage(report.issuer.title);
                $scope.educationInstitutionOptions[issuerTitle] = true;

                findOptionsRecursively(collectOptions, report.learningOpportunitySpecification);
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
            var visible = (!!$scope.educationInstitutionOptions[title]);

            var deselectInvisibleOpportunities = function(opportunity){
                if (opportunity.selected !== undefined) {
                    opportunity.selected = false;
                    selectedCoursesService.removeId(opportunity.elmoIdentifier);
                }
            };

            if (!visible){
                findOptionsRecursively(deselectInvisibleOpportunities, report.learningOpportunitySpecification);
            }
            return visible;

        };

        $scope.sendIds = function () {
            $location.path('preview');
        };
    });
