Declaratively Building A Container Image
-----------------------
The example pipeline here consists of two pieces. The first piece is a build-environment.yaml file. The build-environment.yaml file is a Kubernetes pod specification that describes all containers required to perform your build. In this particular example there are two containers. A JNLP container used to communicate with the Jenkins controller and a Jenkins-Buildah container used to create the new container image. These two containers make up the build environment. A different use case may require additional images, replacing the configured example images with different ones, or creating all new ones specifically designed for your individual purposes. The pipeline will build an image, tag it with the commit hash from source control and push the tagged image to ERCOT's artifact management system. The pipeline finishes by sending an email notification to relevant parties with a link to the build's logs for analysis.

Note: ***ALL*** pod specifcations for Jenkins must include the JNLP container configured exactly as it is in the example.
Note: The example build-environment.yaml include annotations that describe the purpose of the pod and the versions of the tools that are referenced.
Note: Pod specifications should define CPU/RAM limits as is seen in the example. The limits may be adjusted upwards if necessary to accomodate large projects.

Configuration:
-----------------------
  | Name | Type | Required | Description |
  | --- | --- | --- | --- |
  | IMAGE_NAME | String | TRUE | Output container image name. Generally of the form `<business-service>/<business-app>/<component>`|