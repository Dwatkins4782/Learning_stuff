#!/usr/bin/groovy
package com.ercot.build

/**
 * Method to initialize the Helm client on the ephemeral Helm agent.
 **/
def helmInit() {
    addRepository(ConfigurationDefaults.DEFAULT_CHART_REPO, ConfigurationDefaults.DEFAULT_CHART_REPO_URL)
    helmRepoUpdate()
}

/**
 * Method initialize the helm agent's GPG key chain on the ephemeral Helm agent.
 * publicId - Identifier for the Jenkins Credential used to store the GPG public key.
 * privateId - Identifier for the Jenkins Credential used to store the GPG private key.
 **/
def helmInitPki(String publicId, String privateId) {
    withCredentials([file(credentialsId: publicId, variable: 'GPG_PUBLIC_KEY')]) {
        withCredentials([file(credentialsId: privateId, variable: 'GPG_PRIVATE_KEY')]) {
            sh 'gpg2 --import ${GPG_PUBLIC_KEY}'
            sh 'gpg2 --import ${GPG_PRIVATE_KEY}'
            sh 'gpg2 --export-secret-keys > ' + ConfigurationDefaults.KEYSTORE_PATH
        }
    }
}

/**
 * Method add an additional Helm chart repository to the ephemeral Helm agent.
 * name - Unique identifier for the new repository.
 * url - URL where the new repository can be reached (Note: Agents do not have internet access).
 **/
def addRepository(String name=ConfigurationDefaults.DEFAULT_CHART_REPO, String url) {
    sh 'helm repo add ' + name + ' ' + url
}

/**
 * Method remove a Helm chart repository from the ephemeral Helm agent.
 * name - Identifier for the repository being removed.
 **/
def removeRepository(String repo=ConfigurationDefaults.DEFAULT_CHART_REPO) {
    sh 'helm repo remove ' + repo
}

/**
 * Method add a collection of additional Helm chart repositories to the ephemeral Helm agent.
 * depRepos - Name:URL Map<String, String> for the repositories being being added.
 **/
def initializeDependencyRepos(Map<String, String> depRepos) {
    if(depRepos != null) {
        for (repo in depRepos) {
            addRepository(repo.key, repo.value)
        }
    }
}

/**
 * Method that runs a helm repo update command on the ephemeral Helm agent.
 **/
def helmRepoUpdate() {
    sh 'helm repo update'
}

def packageHelmChart(String dir, String version, String credId) {
    packageHelmChart(dir, version, version, credId)
}

def packageHelmChart(String dir, String appVer, String pkgVer, String credId) {
    withCredentials([string(credentialsId: credId, variable: 'GPG_KEY_NAME')]) {
        sh 'cd ' + dir + ' && source /etc/profile > /dev/null 2>&1 && helm dependency update && helm package --app-version ' + appVer + ' --version ' + pkgVer + ' --sign --key "$GPG_KEY_NAME" --keyring /home/jenkins/.gnupg/secring.gpg .'
    }
}

def pushHelmChart(String credId, String repoHost, String repoName, String chartName, String chartVersion) {
    withCredentials([usernamePassword(credentialsId: credId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        sh 'cd ' + chartName + ' && curl --fail --insecure --user $USERNAME:$PASSWORD https://' + repoHost + '/artifactory/' + repoName + '/ --upload-file ' + chartName + '-' + chartVersion + '.tgz'
        sh 'cd ' + chartName + ' && curl --fail --insecure --user $USERNAME:$PASSWORD https://' + repoHost + '/artifactory/' + repoName + '/ --upload-file ' + chartName + '-' + chartVersion + '.tgz.prov'
    }
}

def String constructDeploymentFlags(List<String> modules, String tag, String userFlags) {
    String retval = constructVersionFlags(modules, tag);

    retval += '--set-string application.hash=' + tag + ' '

    if(userFlags != null) {
        retval += userFlags
    }

    return retval;
}

def String constructVersionFlags(List<String> modules, String tag) {
    String retval = ''

    if(modules != null) {
        for (mod in modules) {
            retval += '--set-string ' + mod + '.version=' + tag + ' '
        }
    }

    return retval;
}

def deployHelmChart(String chartName, String chartVersion, String chartRepoName, String releaseName, String configYaml, boolean verify, String namespace, String flags) {
    helmRepoUpdate()

    String helmCommand = "--install --atomic --wait --timeout " + ConfigurationDefaults.DEFAULT_HELM_TIMEOUT + " --cleanup-on-fail --create-namespace";

    if(chartVersion != null && !chartVersion.isEmpty()) {
        helmCommand += " --version " + chartVersion
    }

    if(namespace != null && !namespace.isEmpty()) {
        helmCommand += " --namespace " + namespace
    }

    if(configYaml != null && !configYaml.isEmpty()) {
        helmCommand += " --values " + configYaml
    }

    if(verify) {
        helmCommand += " --verify --keyring " + ConfigurationDefaults.KEYSTORE_PATH
    }

    if(flags != null && !flags.isEmpty()) {
        helmCommand += " " + flags
    }

    sh 'helm upgrade ' + releaseName + ' ' + chartRepoName + '/' + chartName + ' ' + helmCommand
}

return this
