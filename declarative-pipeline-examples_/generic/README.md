Declaratively Running A Generic Build
-----------------------
The example pipeline here consists of two pieces. The first piece is a build-environment.yaml file. The build-environment.yaml file is a Kubernetes pod specification that describes all containers required to perform your build. In this particular example there are two containers. A JNLP container used to communicate with the Jenkins controller and a Jenkins-Base container used to create the new build. These two containers make up the build environment. A different use case may require additional images, replacing the configured example images with different ones, or creating all new ones specifically designed for your individual purposes. The pipeline will run the script `<BUILD_SCRIPT_NAME>` and archive any artifacts in the working directory with the Ant style extension globbing provided here `<ARTIFACT_EXTENSIONS_HERE>`. The pipeline finishes by sending an email notification to relevant parties with a link to the build's logs for analysis.

Note: ***ALL*** pod specifcations for Jenkins must include the JNLP container configured exactly as it is in the example.
Note: The example build-environment.yaml include annotations that describe the purpose of the pod and the versions of the tools that are referenced.
Note: Pod specifications should define CPU/RAM limits as is seen in the example. The limits may be adjusted upwards if necessary to accomodate large projects.
Note: Jenkins only retains two successful builds worth of artifacts for each job at any time.

Configuration:
-----------------------
  | Name | Type | Required | Description |
  | --- | --- | --- | --- |
  | BUILD_SCRIPT_NAME | String | TRUE | Name of the script Jenkins should run. |
  | ARTIFACT_EXTENSIONS | String | TRUE | Comma separated list of file extensions that should be archived i.e. "**/*.jar" |