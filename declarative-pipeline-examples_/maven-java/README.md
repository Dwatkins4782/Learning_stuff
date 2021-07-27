Declaratively Building A Maven Project
-----------------------
The example pipeline here consists of two pieces. The first piece is a build-environment.yaml file. The build-environment.yaml file is a Kubernetes pod specification that describes all containers required to perform your build. In this particular example there are two containers. A JNLP container used to communicate with the Jenkins controller and a Jenkins-Buildah container used to create the new container image. These two containers make up the build environment. A different use case may require additional images, replacing the configured example images with different ones, or creating all new ones specifically designed for your individual purposes. The pipeline will run a dependency analysis on your Maven project then run Maven using the lifecycle goal you provide as an input. The pipeline then attempts to gather results from standard Maven-based unit testing as well as the analysis files from common Java static analysis tools that can be integrated into a Maven build process. The pipeline finishes by sending an email notification to relevant parties with a link to the build's logs for analysis.

Note: ***ALL*** pod specifcations for Jenkins must include the JNLP container configured exactly as it is in the example.
Note: The example build-environment.yaml include annotations that describe the purpose of the pod and the versions of the tools that are referenced.
Note: Pod specifications should define CPU/RAM limits as is seen in the example. The limits may be adjusted upwards if necessary to accomodate large projects.
Note: Jenkins only retains two successful builds worth of artifacts for each job at any time.

Configuration:
-----------------------
  | Name | Type | Required | Description |
  | --- | --- | --- | --- |
  | MAVEN_COMMAND | String | TRUE | Maven lifecycle goal for the build process. i.e. compile, package, deploy |
  | ARTIFACT_EXTENSIONS | String | TRUE | Comma separated list of file extensions that should be archived i.e. "**/*.jar" |


Technical Note
-----------------------
The example pipeline makes the assumption that the Maven project being built does not have any dependencies that cannot be resolved using ERCOT's artifact management system. If your project does have dependencies that cannot be resolved this way you will need to create a more advanced setup. The advanced setup will need to provision a persistent volume in Kubernetes, mount that volume into your pod specification, and use that as a .m2/repository so build processes have access to artifacts that have not been deployed in the artifact management system. 

Understand that this process is frowned upon and the more correct solution is building a self-contained Maven project that is either directly responsible for building everything it needs or retrieves those dependencies from the artifact management system.