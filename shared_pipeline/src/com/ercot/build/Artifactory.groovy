#!/usr/bin/groovy
package com.ercot.build

/**
 * Method that uploads arbitrary artifacts to a given repository in artifactory.
 **/
def uploadArtifact(String credId, String repoHost=ConfigurationDefaults.DEFAULT_ARTIFACT_HOST, String repoName, String path=".", String pattern) {
    withCredentials([usernamePassword(credentialsId: credId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        sh 'find ' + path + ' --name ' + pattern + ' -type f -exec curl --fail --user $USERNAME:$PASSWORD https://' + repoHost + '/artifactory/' + repoName + '/ --upload-file {}'
    }
}

return this
