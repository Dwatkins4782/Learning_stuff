Declaratively Deploying A Helm Chart
-----------------------
The example pipeline here consists of two pieces. The first piece is a build-environment.yaml file. The build-environment.yaml file is a Kubernetes pod specification that describes all containers required to perform your build. In this particular example there are two containers. A JNLP container used to communicate with the Jenkins controller and a Jenkins-Helm container used to create the new container image. These two containers make up the deployment environment. A different use case may require additional images, replacing the configured example images with different ones, or creating all new ones specifically designed for your individual purposes. The pipeline will gather the necessary configuration files from source control, configure the Helm client with ERCOT's Helm repository, login to the specified OpenShift cluster, and deploy the chart. The pipeline finishes by sending an email notification to relevant parties with a link to the build's logs for analysis.

Note: ***ALL*** pod specifcations for Jenkins must include the JNLP container configured exactly as it is in the example.
Note: The example build-environment.yaml include annotations that describe the purpose of the pod and the versions of the tools that are referenced.
Note: Pod specifications should define CPU/RAM limits as is seen in the example. The limits may be adjusted upwards if necessary to accomodate large projects.

Configuration:
-----------------------
  | Name | Type | Required | Description |
  | --- | --- | --- | --- |
  | ItcmIssue | String | TRUE | ITCM RFC/DR Issue Number |
  | DeploymentCluster | String | TRUE | OpenShift cluster name. |
  | ChartRepositoryUrl | String | TRUE | URL for the Helm chart repository. |
  | ChartName | String | TRUE | Name of the Helm chart to deploy. |
  | ChartVersion | String | TRUE | Version number of the Helm chart to deploy. |
  | ReleaseName | String | TRUE | Name of the release the Helm chart should be deployed as. |
  | Namespace | String | TRUE | Kubernetes namespace where the chart should be deployed. |
  | ConfigRepo | String | TRUE | Git repository where Helm chart configuration is stored. |
  | ConfigRepoBranch | String | TRUE | Git repository branch where Helm chart configuration is stored. |

Technical Note
-----------------------
The example pipeline is a parameterized pipeline. It requires manual input in the UI of the Jenkins instance performing the deployment.