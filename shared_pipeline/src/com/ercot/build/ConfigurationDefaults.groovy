package com.ercot.build

public class ConfigurationDefaults {

// Kubernetes Deployment Service Accounts
public static final String DEFAULT_DEPLOY_SERVICE_ACCOUNT = "jenkins-deployer";

// Jenkins Kubernetes Build "Cloud" Name
public static final String DEFAULT_K8S_CLUSTER = "kubernetes";

// Jenkins Credential Identifiers
public static final String DEFAULT_SCM_CREDENTIAL_ID = "JENKINS_SERVICE_ACCOUNT";
public static final String DEFAULT_SCM_SSH_CREDENTIAL_ID = "JENKINS_SERVICE_ACCOUNT_SSH";
public static final String DEFAULT_REGISTRY_CREDENTIAL_ID = "JENKINS_SERVICE_ACCOUNT";
public static final String DEFAULT_CHART_REPO_CREDENTIAL_ID = "JENKINS_SERVICE_ACCOUNT";
public static final String DEFAULT_GPG_SIGNING_KEY_NAME_ID = "JENKINS_GPG_KEY_NAME";
public static final String DEFAULT_GPG_PRIVATE_SIGNING_KEY_FILE_ID = "JENKINS_GPG_PRIVATE_KEY";
public static final String DEFAULT_GPG_PUBLIC_SIGNING_KEY_FILE_ID = "JENKINS_GPG_PUBLIC_KEY";
public static final String DEFAULT_MAPBOX_TOKEN = "JENKINS_MAPBOX_TOKEN";

// Jenkins Artifact Build Destination Registry

public static final String DEFAULT_REGISTRY_HOST = "docker.repo.ercot.com"
public static final String DEFAULT_IMAGE_BASE_CTX = "ercot"

public static final String DEFAULT_CHART_REPO_HOST = "repo.ercot.com";
public static final String DEFAULT_CHART_REPO_NAME = "helm";
public static final String DEFAULT_CHART_REPO = "ercot-charts";
public static final String DEFAULT_CHART_REPO_URL = "https://" + DEFAULT_CHART_REPO_HOST + "/artifactory/" + DEFAULT_CHART_REPO_NAME + "/";

public static final String DEFAULT_ARTIFACT_HOST = "repo.ercot.com";
public static final String DEFAULT_ARTIFACT_REPO_ID = "ercot-artifactory";
public static final String DEFAULT_ARTIFACT_REPO_URL = "https://" + DEFAULT_ARTIFACT_HOST;
public static final String DEFAULT_ARTIFACT_REPO_PATH = "/artifactory/maven";

// Jenkins SCM
public static final String SCM_MASTER_BRANCH = "master";
public static final String SCM_DEVELOP_BRANCH = "develop";
public static final String SCM_RELEASE_BRANCH = "release";

// Jenkins Build Agent Containers
public static final String DEFAULT_JNLP_IMAGE = "docker.repo.ercot.com/jenkins/inbound-agent:latest";
public static final String JNLP_CONTAINER_NAME = "jnlp";

public static final String DEFAULT_BASE_IMAGE = "docker.repo.ercot.com/ercot/jenkins/base-agent:latest";
public static final String BASE_CONTAINER_NAME = "jenkins-base";

public static final String DEFAULT_JAVA_IMAGE = "docker.repo.ercot.com/ercot/jenkins/java-agent/%1\$s:latest";
public static final String JAVA_CONTAINER_NAME = "jenkins-java";
public static final String DEFAULT_JDK_VERSION = "jdk15";

public static final String DEFAULT_BW_IMAGE = "docker.repo.ercot.com/ercot/jenkins/tibco-agent:latest";
public static final String BW_CONTAINER_NAME = "jenkins-tibco";

public static final String DEFAULT_BUILDAH_IMAGE = "docker.repo.ercot.com/ercot/jenkins/buildah-agent:latest";
public static final String BUILDAH_CONTAINER_NAME = "jenkins-buildah";

public static final String DEFAULT_HELM_IMAGE = "docker.repo.ercot.com/ercot/jenkins/helm-agent:latest";
public static final String HELM_CONTAINER_NAME = "jenkins-helm";

public static final String DEFAULT_HELM_DEPLOY_IMAGE = "docker.repo.ercot.com/ercot/tools/openshift/helm-agent:latest";
public static final String HELM_DEPLOYER_CONTAINER_NAME = "jenkins-helm-deployer";

public static final String DEFAULT_SELENIUM_IMAGE = "docker.repo.ercot.com/ercot/jenkins/selenium-agent:latest";
public static final String SELENIUM_CONTAINER_NAME = "jenkins-selenium";

public static final String DEFAULT_MAPBOX_IMAGE = "docker.repo.ercot.com/ercot/jenkins/mapbox-agent:latest";
public static final String MAPBOX_CONTAINER_NAME = "jenkins-mapbox";

public static final String CONTAINER_EMPTYDIR_PATH = "/var/lib/containers";

// Jenkins Stage Names
public static final String CHECKOUT_STAGE = "Checkout";
public static final String IMAGE_BUILD_PUSH_STAGE = "Image Build/Push";
public static final String GENERIC_BUILD_STAGE = "Generic Build";
public static final String BW_BUILD_STAGE = "Tibco Build";
public static final String MAPBOX_FETCH_STAGE = "Fetch Atlas Enterprise Tilesets";
public static final String MAPBOX_UPLOD_STAGE = "Upload Atlas Enterprise Tilesets";
public static final String ARCHIVE_STAGE = "Archive";
public static final String HELM_PACKAGE_STAGE = "Helm Chart Packaging";
public static final String HELM_DEPLOY_STAGE = "Helm Chart Deployment";
public static final String MAVEN_DEPENDENCY_STAGE = "Dependency Report";
public static final String MAVEN_VERSION_STAGE = "Set Version";
public static final String MAVEN_BUILD_STAGE = "Maven Build";
public static final String STATIC_ANALYSIS_STAGE = "Static/Test Analysis";
public static final String DEPLOY_STAGE = "Deploy";
public static final String SMOKE_TEST_STAGE = "Smoke Test";
public static final String CLUSTER_LOGIN = "OpenShift Cluster Login";
public static final String CLUSTER_LOGOUT = "OpenShift Cluster Logout";

// Jenkins Static Analysis
public static final String JUNIT_TEST_RESULTS = "**/target/surefire-reports/TEST-*.xml";
public static final String JACOCO_EXCLUSIONS = "**/generated/**/*";
public static final String CHECKSTYLE_RESULTS = "**/checkstyle-result.xml";
public static final String SPOTBUGS_RESULTS = "**/spotbugsXml.xml";
public static final String CPD_RESULTS = "**/cpd.xml";
public static final String PMD_RESULTS = "**/pmd.xml";

// Generic Tools
public static final String DEFAULT_FILE_DATE_FORMAT = "date +%Y%m%d%I%M%S";

// Generic Build
public static final String GENERIC_BUILD_COMMAND = "find . -type f -path .git -prune -o -exec dos2unix {} \\; && chmod +x build && ./build";

// Tibco Build
public static final String BW_BUILD_COMMAND = "find . -type f -path .git -prune -o -exec dos2unix {} \\; && chmod +x build && ./build";

// Maven Deployment
public static final String CONFIG_FILE_PATH = "helm-config";
public static final String CONFIG_FILE_NAME = "configuration.yaml";
public static final String SMOKE_TEST_PATH = "smoke";
public static final String DEFAULT_MAVEN_SETTINGS_FILE = "/home/jenkins/.m2/ercot-artifactory.xml";
public static final String DEFAULT_ARCHIVE_PATTERN = "**/*.war, **/*.jar, **/*.tar.gz, **/*.zip, **/*.ear, **/*.tgz, **/*.tgz.prov";
public static final String JAVA_MAVEN_CACHE_PATH = "/home/jenkins/.m2/repository";
public static final String JAVA_MAVEN_CACHE = "%1\$s-jcasc-jmc";

// Helm Deployment
public static final String DEFAULT_NAMESPACE_SUFFIX = "-cicd";
public static final String DEFAULT_HELM_CONFIG_FILE = "configuration.yaml";
public static final String DEFAULT_HELM_CONFIG_BRANCH = "master";
public static final String DEFAULT_HELM_TIMEOUT = "1200s";
public static final String KEYSTORE_PATH = "/home/jenkins/.gnupg/secring.gpg";

}
