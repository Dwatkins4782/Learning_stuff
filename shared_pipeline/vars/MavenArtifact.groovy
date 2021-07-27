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
        containerTemplate(name: ConfigurationDefaults.JAVA_CONTAINER_NAME, image: (config.javaImage != null) ? config.javaImage : ( config.jdk != null ? String.format(ConfigurationDefaults.DEFAULT_JAVA_IMAGE, config.jdk) : String.format(ConfigurationDefaults.DEFAULT_JAVA_IMAGE, ConfigurationDefaults.DEFAULT_JDK_VERSION) ), alwaysPullImage: true, ttyEnabled: true, runAsUser: "1000", runAsGroup: "1000",    envVars: [ envVar(key: 'http_proxy', value: 'http://devfwd.ercot.com:7090'), envVar(key: 'https_proxy', value: 'http://devfwd.ercot.com:7090'), envVar(key: 'no_proxy', value: 'localhost,*.ercot.com')])
    ],
    volumes: [
        persistentVolumeClaim(mountPath: ConfigurationDefaults.JAVA_MAVEN_CACHE_PATH, claimName: String.format(ConfigurationDefaults.JAVA_MAVEN_CACHE, env.JENKINS_URL.split("\\.")[1]))
    ]){
        node(POD_LABEL) {

            Maven maven = new Maven()

            stage (ConfigurationDefaults.CHECKOUT_STAGE) {
                checkout scm
            }

            stage (ConfigurationDefaults.MAVEN_DEPENDENCY_STAGE) {
                container(ConfigurationDefaults.JAVA_CONTAINER_NAME) {
                    maven.dependencies(config.mvnFlags)
                }
            }

            stage (ConfigurationDefaults.MAVEN_BUILD_STAGE) {
                container(ConfigurationDefaults.JAVA_CONTAINER_NAME) {
                    if((env.BRANCH_NAME).startsWith(ConfigurationDefaults.SCM_RELEASE_BRANCH)) {
                        maven.deploy(config.mvnFlags)
                    } else {
                        maven.install(config.mvnFlags)
                    }
                }
            }

            stage(ConfigurationDefaults.STATIC_ANALYSIS_STAGE) {
                jacoco exclusionPattern: ConfigurationDefaults.JACOCO_EXCLUSIONS
                recordIssues enabledForFailure: true, tools: [mavenConsole(), java(), javaDoc(), junitParser(pattern: ConfigurationDefaults.JUNIT_TEST_RESULTS), checkStyle(pattern: ConfigurationDefaults.CHECKSTYLE_RESULTS), spotBugs(useRankAsPriority: true, pattern: ConfigurationDefaults.SPOTBUGS_RESULTS), cpd(pattern: ConfigurationDefaults.CPD_RESULTS), pmdParser(pattern: ConfigurationDefaults.PMD_RESULTS)]
            }

            stage(ConfigurationDefaults.ARCHIVE_STAGE) {
                archiveArtifacts artifacts: (config.archive != null) ? config.archive : ConfigurationDefaults.DEFAULT_ARCHIVE_PATTERN, allowEmptyArchive: true
            }
        }
    }
}
