app = angular.module('smp', ['ngRoute', 'ngCookies']);

app.config(function ($routeProvider, $httpProvider, $locationProvider) {

    $routeProvider.
        when('/', {
            templateUrl: 'partials/select_ncp.html',
            controller: 'home'
        }).
        otherwise({
            redirectTo: '/'
        });

    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
});

app.controller('home', function ($scope, $http, $sce, $cookies, $timeout) {

    var countryFlags =
    {
        Finland: 'finland.png',
        Denmark: 'denmark.png',
        Italy: 'italy.png',
        Norway: 'norway.png',
        Sweden: 'sweden.png',
        Poland: 'poland.png'
    };

    $scope.ncpSelectionState = 'country';

    $scope.moveToNcp = function (ncp) {
        $scope.ncpUrl = $sce.trustAsResourceUrl(ncp.url);
        $http.post('api/sessiondata', {url: ncp.url}).success(function (response) {
            $cookies.elmoSessionId = response.sessionId;
            $cookies.chosenNCP = ncp.url;
            $cookies.chosenCert = response.ncpPublicKey;
            $scope.sessionData = response;
            $timeout(function(){
                angular.element('#postSessionData').trigger('click');
            });
        });
    };

    $scope.cancelNcpSelection = function () {
        $scope.ncpSelectionState = 'country';
    };

    $scope.getFlag = function (countryName) {
        for (var country in countryFlags)
            if (countryName.indexOf(country) >= 0)
                return 'flags/' + countryFlags[country];
        return null;
    };

    $scope.selectCountry = function (country) {
        var ncps = $scope.emreg.ncps.filter(function (ncp) {
            return ncp.countryCode == country.countryCode;
        });

        $scope.ncps = ncps;

        if (ncps.length > 1) {
            $scope.ncpSelectionState = 'ncp';
            $scope.availableNcps = ncps;
            console.log(ncps);
        } else if (ncps.length === 1) {
            $scope.moveToNcp(ncps[0]);
        }
    };

    $http.get('/smp/api/emreg').success(function (data) {
        $scope.emreg = data;
    })

});



