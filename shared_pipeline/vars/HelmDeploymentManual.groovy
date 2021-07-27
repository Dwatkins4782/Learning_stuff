#!/usr/bin/groovy
import com.ercot.build.ConfigurationDefaults
import com.ercot.build.Helm

def call(Closure body)
{
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body()

    podTemplate(cloud: (params.Cloud != null) ? params.Cloud : ConfigurationDefaults.DEFAULT_K8S_CLUSTER,
    containers: [
        containerTemplate(name: ConfigurationDefaults.JNLP_CONTAINER_NAME, image: ConfigurationDefaults.DEFAULT_JNLP_IMAGE, alwaysPullImage: true),        
        containerTemplate(name: ConfigurationDefaults.HELM_DEPLOYER_CONTAINER_NAME, image: (params.BuildImage != null) ? params.BuildImage : ConfigurationDefaults.DEFAULT_HELM_DEPLOY_IMAGE, alwaysPullImage: true, ttyEnabled: true)
    ]){
        node(POD_LABEL) {

            Oc client = new Oc()

            stage (ConfigurationDefaults.CHECKOUT_STAGE) {
                if(params.ConfigRepo) {
                    git url: params.ConfigRepo, branch: params.ConfigRepoBranch, credentialsId: ConfigurationDefaults.DEFAULT_SCM_CREDENTIAL_ID
                }
            }

            stage (ConfigurationDefaults.CLUSTER_LOGIN) {
                container(ConfigurationDefaults.HELM_DEPLOYER_CONTAINER_NAME) {
                    //Login to the selected cluster.
                    oc.login(params.Cluster, params.AdminCredId)
                }
            }

            stage (ConfigurationDefaults.HELM_DEPLOY_STAGE) {
                container(ConfigurationDefaults.HELM_DEPLOYER_CONTAINER_NAME) {
                    // Deploy the specified chart.
                    Helm chart = new Helm()
                    chart.addRepository(params.ChartName, params.ChartRepository)
                    chart.deployHelmChart(params.ChartName, params.ChartVersion, params.ChartName, params.ChartName, params.ChartYaml, params.Verify, params.Namespace, params.Flags)
                }
            }

            stage (ConfigurationDefaults.CLUSTER_LOGOUT) {
                container(ConfigurationDefaults.HELM_DEPLOYER_CONTAINER_NAME) {
                    //Logout from the selected cluster.
                    oc.logout(params.Cluster)
                }
            }
        }
    }
}