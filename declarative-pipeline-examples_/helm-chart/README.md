Declaratively Building A Helm Chart
-----------------------
The example pipeline here consists of two pieces. The first piece is a build-environment.yaml file. The build-environment.yaml file is a Kubernetes pod specification that describes all containers required to perform your build. In this particular example there are two containers. A JNLP container used to communicate with the Jenkins controller and a Jenkins-Helm container used to create the new helm chart. These two containers make up the build environment. A different use case may require additional images, replacing the configured example images with different ones, or creating all new ones specifically designed for your individual purposes. The pipeline will build the helm chart, push it to ERCOT's artifact management system, and archive the chart's tgz file locally in Jenkins. The pipeline finishes by sending an email notification to relevant parties with a link to the build's logs for analysis.

Note: ***ALL*** pod specifcations for Jenkins must include the JNLP container configured exactly as it is in the example.
Note: The example build-environment.yaml include annotations that describe the purpose of the pod and the versions of the tools that are referenced.
Note: Pod specifications should define CPU/RAM limits as is seen in the example. The limits may be adjusted upwards if necessary to accomodate large projects.
Note: Jenkins only retains two successful builds worth of artifacts for each job at any time.

Configuration: N/A
-----------------------