#!/usr/bin/groovy
import com.ercot.build.ConfigurationDefaults
import com.ercot.build.Helm

def call(Closure body)
{
    Map config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    podTemplate(cloud: (config.cloud != null) ? config.cloud : ConfigurationDefaults.DEFAULT_K8S_CLUSTER,
    containers: [
        containerTemplate(name: ConfigurationDefaults.JNLP_CONTAINER_NAME, image: ConfigurationDefaults.DEFAULT_JNLP_IMAGE, alwaysPullImage: true),
        containerTemplate(name: ConfigurationDefaults.HELM_CONTAINER_NAME, image: (config.buildImage != null) ? config.buildImage : ConfigurationDefaults.DEFAULT_HELM_IMAGE, alwaysPullImage: true, ttyEnabled: true)
    ]){
        node(POD_LABEL) {

            stage (ConfigurationDefaults.CHECKOUT_STAGE) {
                dir(config.chartName) {
                    checkout scm
                }
            }

            stage (ConfigurationDefaults.HELM_PACKAGE_STAGE) {
                container(ConfigurationDefaults.HELM_CONTAINER_NAME) {
                    // Create, version, and sign helm chart package.
                    Helm chart = new Helm()
                    chart.helmInit()
                    chart.helmInitPki((config.publicKeyFileId != null) ? config.publicKeyFileId : ConfigurationDefaults.DEFAULT_GPG_PUBLIC_SIGNING_KEY_FILE_ID, (config.privateKeyFileId != null) ? config.privateKeyFileId : ConfigurationDefaults.DEFAULT_GPG_PRIVATE_SIGNING_KEY_FILE_ID)
                    chart.initializeDependencyRepos(config.depRepos)
                    chart.packageHelmChart(config.chartName, config.appVersion, config.chartVersion, (config.keyNameId != null) ? config.keyNameId : ConfigurationDefaults.DEFAULT_GPG_SIGNING_KEY_NAME_ID)
                    chart.pushHelmChart((config.acctId != null) ? config.acctId : ConfigurationDefaults.DEFAULT_CHART_REPO_CREDENTIAL_ID, (config.repoHost != null) ? config.repoHost : ConfigurationDefaults.DEFAULT_CHART_REPO_HOST, (config.repoName != null) ? config.repoName : ConfigurationDefaults.DEFAULT_CHART_REPO_NAME, config.chartName, config.chartVersion)
                }
            }

            stage(ConfigurationDefaults.ARCHIVE_STAGE) {
                archiveArtifacts artifacts: (config.archive != null) ? config.archive : ConfigurationDefaults.DEFAULT_ARCHIVE_PATTERN, allowEmptyArchive: true
            }
        }
    }
}
