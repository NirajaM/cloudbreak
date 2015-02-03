'use strict';

var log = log4javascript.getLogger("templateController-logger");

angular.module('uluwatuControllers').controller('templateController', ['$scope', '$rootScope', '$filter', 'UserTemplate', 'AccountTemplate', 'GlobalTemplate',
    function ($scope, $rootScope, $filter, UserTemplate, AccountTemplate, GlobalTemplate) {

        $rootScope.templates = AccountTemplate.query();
        $scope.awsTemplateForm = {};
        $scope.gccTemplateForm = {};
        $scope.openstackTemplateForm = {};
        initializeAzureTemp();
        initializeAwsTemp();
        initializeGccTemp();
        initializeOpenstackTemp();

        $scope.createAwsTemplateRequest = function () {
            $scope.azureTemplate = false;
            $scope.awsTemplate = true;
            $scope.gccTemplate = false;
            $scope.openstackTemplate = false;
        }

        $scope.createAzureTemplateRequest = function () {
            $scope.azureTemplate = true;
            $scope.awsTemplate = false;
            $scope.gccTemplate = false;
            $scope.openstackTemplate = false;
        }

        $scope.createGccTemplateRequest = function () {
            $scope.azureTemplate = false;
            $scope.awsTemplate = false;
            $scope.gccTemplate = true;
            $scope.openstackTemplate = false;
        }

        $scope.createOpenstackTemplateRequest = function () {
          $scope.azureTemplate = false;
          $scope.awsTemplate = false;
          $scope.gccTemplate = false;
          $scope.openstackTemplate = true;
        }

        $scope.createAwsTemplate = function () {
            $scope.awsTemp.cloudPlatform = 'AWS';
            $scope.awsTemp.parameters.sshLocation = '0.0.0.0/0';
            $scope.awsTemp.parameters.amiId = ($filter('filter')($rootScope.config.AWS.amis, { key: $scope.awsTemp.parameters.region})[0]).value;
            if ($scope.awsTemp.public) {
                AccountTemplate.save($scope.awsTemp, function (result) {
                    handleAwsTemplateSuccess(result)
                }, function (error) {
                    handleTemplateError(error, $rootScope.error_msg.aws_template_failed)
                });
            } else {
                UserTemplate.save($scope.awsTemp, function (result) {
                    handleAwsTemplateSuccess(result)
                }, function (error) {
                    handleTemplateError(error, $rootScope.error_msg.aws_template_failed)
                });
            }

            function handleAwsTemplateSuccess(result) {
                $scope.awsTemp.id = result.id;
                $rootScope.templates.push($scope.awsTemp);
                initializeAwsTemp();
                $scope.modifyStatusMessage($rootScope.error_msg.aws_template_success1 + result.id + $rootScope.error_msg.aws_template_success2);
                $scope.modifyStatusClass("has-success");
                $scope.awsTemplateForm.$setPristine();
                collapseCreateTemplateFormPanel();
            }
        }

        $scope.createOpenstackTemplate = function () {
          $scope.openstackTemp.cloudPlatform = 'OPENSTACK';
          if ($scope.openstackTemp.public) {
            AccountTemplate.save($scope.openstackTemp, function (result) {
              handleOpenstackTemplateSuccess(result)
            }, function (error) {
              handleOpenstackTemplateError(error)
              handleTemplateError(error, $rootScope.error_msg.openstack_template_failed)
            });
          } else {
            UserTemplate.save($scope.openstackTemp, function (result) {
              handleOpenstackTemplateSuccess(result)
            }, function (error) {
              handleOpenstackTemplateError(error)
              handleTemplateError(error, $rootScope.error_msg.openstack_template_failed)
            });
          }

          function handleOpenstackTemplateSuccess(result) {
            $scope.openstackTemp.id = result.id;
            $rootScope.templates.push($scope.openstackTemp);
            initializeOpenstackTemp();
            $scope.modifyStatusMessage($rootScope.error_msg.openstack_template_success1 + result.id + $rootScope.error_msg.openstack_template_success2);
            $scope.modifyStatusClass("has-success");
            $scope.openstackTemplateForm.$setPristine();
            collapseCreateTemplateFormPanel();
          }
          
        }

        $scope.createGccTemplate = function () {
            $scope.gccTemp.cloudPlatform = 'GCC';
            if ($scope.gccTemp.public) {
                AccountTemplate.save($scope.gccTemp, function (result) {
                    handleGccTemplateSuccess(result)
                }, function (error) {
                    handleGccTemplateError(error)
                    handleTemplateError(error, $rootScope.error_msg.gcc_template_failed)
                });
            } else {
                UserTemplate.save($scope.gccTemp, function (result) {
                    handleGccTemplateSuccess(result)
                }, function (error) {
                    handleGccTemplateError(error)
                    handleTemplateError(error, $rootScope.error_msg.gcc_template_failed)
                });
            }

            function handleGccTemplateSuccess(result) {
                $scope.gccTemp.id = result.id;
                $rootScope.templates.push($scope.gccTemp);
                initializeGccTemp();
                $scope.modifyStatusMessage($rootScope.error_msg.gcc_template_success1 + result.id + $rootScope.error_msg.gcc_template_success2);
                $scope.modifyStatusClass("has-success");
                $scope.gccTemplateForm.$setPristine();
                collapseCreateTemplateFormPanel();
            }
        }

        $scope.createAzureTemplate = function () {
            $scope.azureTemp.cloudPlatform = "AZURE";
            $scope.azureTemp.parameters.imageName = "ambari-docker-v1";
            if($scope.azureTemp.public) {
                AccountTemplate.save($scope.azureTemp, function (result) {
                  handleAzureTemplateSuccess(result)
               }, function (error) {
                  handleTemplateError(error, $rootScope.error_msg.azure_template_failed)
               });
            } else {
               UserTemplate.save($scope.azureTemp, function (result) {
                  handleAzureTemplateSuccess(result)
               }, function (error) {
                  handleTemplateError(error, $rootScope.error_msg.azure_template_failed)
               });
            }

            function handleAzureTemplateSuccess(result) {
                $scope.azureTemp.id = result.id;
                $rootScope.templates.push($scope.azureTemp);
                initializeAzureTemp();
                $scope.modifyStatusMessage($rootScope.error_msg.azure_template_success1 + result.id + $rootScope.error_msg.azure_template_success2);
                $scope.modifyStatusClass("has-success");
                $scope.azureTemplateForm.$setPristine();
                collapseCreateTemplateFormPanel();
            }
        }

        $scope.deleteTemplate = function (template) {
            GlobalTemplate.delete({ id: template.id }, function (success) {
                $rootScope.templates.splice($rootScope.templates.indexOf(template), 1);
                $scope.modifyStatusMessage($rootScope.error_msg.template_delete_success1 + template.id + $rootScope.error_msg.template_delete_success2);
                $scope.modifyStatusClass("has-success");
            }, function (error) {
                $scope.modifyStatusMessage($rootScope.error_msg.template_delete_failed + ": " + error.data.message);
                $scope.modifyStatusClass("has-error");
            });
        }
        
        function handleTemplateError(error, templateFailedMsg) {
            var errorData = error.data["validationErrors"]
            if (errorData != null) {
               for (var key in errorData) {
                  templateFailedMsg += errorData[key] + "; "
               }
            } else {
                  templateFailedMsg += ": " + error.data.message
            }
            $scope.modifyStatusMessage(templateFailedMsg);
            $scope.modifyStatusClass("has-error");
         }

        function collapseCreateTemplateFormPanel() {
          angular.element(document.querySelector('#panel-create-templates-collapse-btn')).click();
        }

        function initializeAwsTemp() {
            $scope.awsTemp = {
                parameters: {
                    sshLocation: "0.0.0.0/0",
                    instanceType: "T2Medium",
                    volumeType: "Standard"
                }
            }
        }

        function initializeAzureTemp() {
            $scope.azureTemp = {
                parameters: {
                    vmType: "MEDIUM"
                }
            }
        }

        function initializeOpenstackTemp() {
          $scope.openstackTemp = {
            parameters: {
            }
          }
        }

        function initializeGccTemp() {
            $scope.gccTemp = {
                parameters: {
                    gccInstanceType: "N1_STANDARD_2",
                    volumeType: "HDD"
                }
            }
        }
    }
]);
