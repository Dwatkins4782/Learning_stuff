#!/usr/bin/groovy
import com.ercot.build.ConfigurationDefaults

def call(Closure body)
{
    Map config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    podTemplate(cloud: (config.cloud != null) ? config.cloud : ConfigurationDefaults.DEFAULT_K8S_CLUSTER,
    containers: [
        containerTemplate(name: ConfigurationDefaults.JNLP_CONTAINER_NAME, image: ConfigurationDefaults.DEFAULT_JNLP_IMAGE, alwaysPullImage: true),
        containerTemplate(name: ConfigurationDefaults.BW_CONTAINER_NAME, image: (config.buildImage != null) ? config.buildImage : ConfigurationDefaults.DEFAULT_BW_IMAGE, alwaysPullImage: true, ttyEnabled: true)
    ]){
        node(POD_LABEL) {

            stage (ConfigurationDefaults.CHECKOUT_STAGE) {
                checkout scm
            }

            stage (ConfigurationDefaults.BW_BUILD_STAGE) {
                container(ConfigurationDefaults.BW_CONTAINER_NAME) {
                    sh ConfigurationDefaults.BW_BUILD_COMMAND
                }
            }

            stage(ConfigurationDefaults.ARCHIVE_STAGE) {
                archiveArtifacts artifacts: (config.archive != null) ? config.archive : ConfigurationDefaults.DEFAULT_ARCHIVE_PATTERN, allowEmptyArchive: true
            }
        }
    }
}
