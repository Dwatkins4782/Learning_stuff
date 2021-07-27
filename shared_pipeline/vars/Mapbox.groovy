#!/usr/bin/groovy
import com.ercot.build.ConfigurationDefaults
import com.ercot.build.Maven

def call(Closure body)
{
    Map config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    podTemplate(cloud: (config.cloud != null) ? config.cloud : ConfigurationDefaults.DEFAULT_K8S_CLUSTER,
    containers: [
        containerTemplate(name: ConfigurationDefaults.JNLP_CONTAINER_NAME, image: ConfigurationDefaults.DEFAULT_JNLP_IMAGE, alwaysPullImage: true),
        containerTemplate(name: ConfigurationDefaults.MAPBOX_CONTAINER_NAME, image: (config.buildImage != null) ? config.buildImage : ConfigurationDefaults.DEFAULT_MAPBOX_IMAGE, alwaysPullImage: true, ttyEnabled: true)
    ],
    workspaceVolume: persistentVolumeClaimWorkspaceVolume(claimName: 'mapboxatlastilesetclaim')) {
        node(POD_LABEL) {

            String dateStamp = sh(returnStdout: true, script: ConfigurationDefaults.DEFAULT_FILE_DATE_FORMAT).trim()

            stage (ConfigurationDefaults.MAPBOX_FETCH_STAGE) {
                container(ConfigurationDefaults.MAPBOX_CONTAINER_NAME) {

                    withCredentials([string(credentialsId: ConfigurationDefaults.DEFAULT_MAPBOX_TOKEN, variable: 'MAPBOX_ACCESS_TOKEN')]) {
                        timeout(time: 48, unit: 'HOURS')
                        {
                            Maven mvn = new Maven()
                            sh "source /etc/profile && atlas-installer download mapbox --tilesets all --dev --token '$MAPBOX_ACCESS_TOKEN'"
                            sh "cd mapbox/tilesets && tar cvf atlas-tilesets-'${dateStamp}'.tar *.mbtiles"
                            mvn.deployFile("", "com.mapbox.atlas", "tilesets", dateStamp, "prod", ".tar", "mapbox/tilesets/atlas-tilesets-" + dateStamp + ".tar")
                        }
                    }
                }
            }
        }
    }
}
