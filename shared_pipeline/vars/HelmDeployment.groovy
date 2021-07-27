#!/usr/bin/groovy
import com.ercot.build.ConfigurationDefaults
import com.ercot.build.Helm

def call(Closure body)
{
    Map config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    podTemplate(cloud: (config.cloud != null) ? config.cloud : ConfigurationDefaults.DEFAULT_K8S_CLUSTER, serviceAccount: (params.ServiceAccount != null) ? params.ServiceAccount : ConfigurationDefaults.DEFAULT_DEPLOY_SERVICE_ACCOUNT,
    containers: [
        containerTemplate(name: ConfigurationDefaults.JNLP_CONTAINER_NAME, image: ConfigurationDefaults.DEFAULT_JNLP_IMAGE, alwaysPullImage: true),
        containerTemplate(name: ConfigurationDefaults.HELM_CONTAINER_NAME, image: (config.buildImage != null) ? config.buildImage : ConfigurationDefaults.DEFAULT_HELM_IMAGE, alwaysPullImage: true, ttyEnabled: true)
    ]){
        node(POD_LABEL) {

            stage (ConfigurationDefaults.CHECKOUT_STAGE) {
                if(config.configRepo) {
                    git url: config.configRepo, branch: (config.configRepoBranch != null) ? config.configRepoBranch : ConfigurationDefaults.DEFAULT_HELM_CONFIG_BRANCH, credentialsId: ConfigurationDefaults.DEFAULT_SCM_SSH_CREDENTIAL_ID
                }
            }

            stage (ConfigurationDefaults.HELM_DEPLOY_STAGE) {
                container(ConfigurationDefaults.HELM_CONTAINER_NAME) {
                    // Deploy the specified chart.

                    Helm chart = new Helm()
                    chart.addRepository(config.chartName, (config.chartRepository != null) ? config.chartRepository : ConfigurationDefaults.DEFAULT_CHART_REPO_URL)
                    chart.deployHelmChart(config.chartName,
                                          (config.chartVersion != null) ? config.chartVersion : null,
                                          config.chartName,
                                          config.chartName,
                                          (config.chartYaml != null) ? config.chartYaml : ConfigurationDefaults.CONFIG_FILE_NAME,
                                          false,
                                          (config.namespace != null) ? config.namespace : config.chartName + ConfigurationDefaults.DEFAULT_NAMESPACE_SUFFIX,
                                          config.flags)
                }
            }
        }
    }
}
