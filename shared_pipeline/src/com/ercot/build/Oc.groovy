#!/usr/bin/groovy
package com.ercot.build

/**
 * Login to the specified OpenShift cluster.
 **/
def login(String credId, String clusterName) {
    withCredentials([usernamePassword(credentialsId: credId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        sh 'oc login api.' + clusterName + ':6443 --username=$USERNAME --password=$PASSWORD --insecure-skip-tls-verify=false' 
    }
}

/**
 * Logout from the specified OpenShift cluster.
 **/
def logout(String clusterName) {
    withCredentials([usernamePassword(credentialsId: credId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        sh 'oc logout'
    }
}

return this
