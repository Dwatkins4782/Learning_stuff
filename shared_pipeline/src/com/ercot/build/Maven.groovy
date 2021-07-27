#!/usr/bin/groovy
package com.ercot.build

/**
 * Method to run an arbitrary maven command.
 * command -- Arbitrary maven command to run.
 * flags -- Additional command line flags to add to the maven command.
 **/
def command(String command, String flags) {
    if(flags != null && !flags.isEmpty()) {
        sh "mvn --batch-mode --show-version --fail-at-end " + flags + " " + command
    } else {
        sh "mvn --batch-mode --show-version --fail-at-end " + command
    }
}

/**
 * Method to use the versions plugin to analyze available updates for depencies.
 * flags -- Additional command line flags to add to the maven command.
 **/
def dependencies(String flags) {
    command("versions:display-property-updates --no-snapshot-updates", flags)
}

/**
 * Method to use the versions plugin to set the version in project pom files.
 * version -- Version number the versions plugin should update the project poms to.
 * flags -- Additional command line flags to add to the maven command.
 **/
def version(String version, String flags) {
    command("versions:set -DgenerateBackupPoms=false -DnewVersion=" + version, flags)
}

/**
 * Method to run the maven package command.
 * flags -- Additional command line flags to add to the maven command.
 **/
def pkg(String flags) {
    command("package", flags)
}

/**
 * Method to run the maven install command.
 * flags -- Additional command line flags to add to the maven command.
 **/
def install(String flags) {
    command("install", flags)
}

/**
 * Method to run the maven deploy command.
 * flags -- Additional command line flags to add to the maven command.
 **/
def deploy(String flags, String deploymentRepoId=ConfigurationDefaults.DEFAULT_ARTIFACT_REPO_ID, String deploymentRepoUrl=ConfigurationDefaults.DEFAULT_ARTIFACT_REPO_URL + ConfigurationDefaults.DEFAULT_ARTIFACT_REPO_PATH) {
    withCredentials([usernamePassword(credentialsId: ConfigurationDefaults.DEFAULT_SCM_CREDENTIAL_ID, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        command("-DaltDeploymentRepository=" + deploymentRepoId + "::" + deploymentRepoUrl + " deploy", flags)
    }
}

/**
 * Method to run the maven deploy:deploy-file command.
 * flags -- Additional command line flags to add to the maven command.
 **/
def deployFile(String flags, String groupId, String artifactId, String version, String classifier, String packaging, String file, String deploymentRepoId=ConfigurationDefaults.DEFAULT_ARTIFACT_REPO_ID, String deploymentRepoUrl=ConfigurationDefaults.DEFAULT_ARTIFACT_REPO_URL + ConfigurationDefaults.DEFAULT_ARTIFACT_REPO_PATH) {
    withCredentials([usernamePassword(credentialsId: ConfigurationDefaults.DEFAULT_SCM_CREDENTIAL_ID, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        command("-DgroupId=" + groupId + " -DartifactId=" + artifactId + " -Dversion=" + version + " -Dclassifier=" + classifier + " -Dpackaging=" + packaging + " -Dfile=" + file + " -Durl=" + deploymentRepoUrl + " deploy", flags)
    }
}

/**
 * Method to the maven test command.
 * flags -- Additional command line flags to add to the maven command.
 **/
def test(String flags) {
    command("test", flags)
}

return this
