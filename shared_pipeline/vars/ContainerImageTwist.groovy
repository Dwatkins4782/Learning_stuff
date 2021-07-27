#!/usr/bin/groovy
import com.ercot.build.ConfigurationDefaults
import com.ercot.build.SourceControl
import com.ercot.build.Buildah

def call(Closure body)
{
    Map config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    podTemplate(cloud: (config.cloud != null) ? config.cloud : ConfigurationDefaults.DEFAULT_K8S_CLUSTER,
    containers: [
        containerTemplate(name: ConfigurationDefaults.JNLP_CONTAINER_NAME, image: ConfigurationDefaults.DEFAULT_JNLP_IMAGE, alwaysPullImage: true),
        containerTemplate(name: ConfigurationDefaults.BUILDAH_CONTAINER_NAME, image: (config.buildImage != null) ? config.buildImage : ConfigurationDefaults.DEFAULT_BUILDAH_IMAGE, alwaysPullImage: true, ttyEnabled: true, privileged: true)
    ],
    volumes: [
        emptyDirVolume(mountPath: ConfigurationDefaults.CONTAINER_EMPTYDIR_PATH, memory: true)
    ]){
        node(POD_LABEL){
            List<String> tags = new ArrayList<String>()

            stage (ConfigurationDefaults.CHECKOUT_STAGE) {
                checkout scm
                tags = new SourceControl().getContainerTags()
            }

            stage (ConfigurationDefaults.IMAGE_BUILD_PUSH_STAGE) {
                container(ConfigurationDefaults.BUILDAH_CONTAINER_NAME) {
                    Buildah build = new Buildah()
                    build.buildModule((config.acctId != null) ? config.acctId : ConfigurationDefaults.DEFAULT_REGISTRY_CREDENTIAL_ID, (config.registry != null) ? config.registry : ConfigurationDefaults.DEFAULT_REGISTRY_HOST, config.product, config.module, tags)
                }
            }
            
            stage('twistlock-scan') {
                prismaCloudScanImage ca: '',
                cert: '',
                image: config.product,
                key: '',
                logLevel: 'info',
                podmanPath: 'podman',
                project: '',
                resultsFile: '/tmp/prisma-cloud-scan-results.json',
                ignoreImageBuildTime: true
            }

            stage('twistlock-report-file') {
                prismaCloudPublish resultsFilePattern: '/tmp/prisma-cloud-scan-results.json'
            }
        }
    }
}
