#!/usr/bin/groovy
package com.ercot.build

/**
 * Method to tag a container image.
 * oldName -- Original name of the image being tagged.
 * newName -- Desired name of the image being tagged.
 **/
def tag(String oldName, String newName) {
    sh 'buildah tag ' + oldName + ' ' + newName
}

/**
 * Method to pull a container image.
 * registry -- The container registry host the image should be pulled from.
 * image -- The fully qualified name (name:tag, name:hash) of the image that should be pulled.
 **/
def pull(String registry, String image) {
    sh 'buildah pull --quiet --tls-verify ' + registry + '/' + image
}

/**
 * Method to push a container image.
 * acctId - Identifier for the Jenkins Credential used to login to the registry.
 * registry -- The container registry host the image should be pulled from.
 * image -- The name of the image that should be pushed.
 * tags -- A list of tags associated with the specified image.
 **/
def push(String acctId, String registry, String image, List<String> tags) {
      withCredentials([usernamePassword(credentialsId: acctId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        for(String tag : tags) {
            sh 'buildah push --quiet --creds ${USERNAME}:${PASSWORD} ' + registry + '/' + image + ':' + tag
        }
    }
}

/**
 * Method to build a container image.
 * acctId - Identifier for the Jenkins Credential used to login to the registry.
 * registry -- The container registry host the image should be built for.
 * image -- The name of the image that should be build.
 * tags -- A list of tags associated with the specified image.
 **/
def build(String acctId, String registry, String image, List<String> tags) {
    if(tags != null) {
        String tagString = ''
        for(String tag : tags) {
            tagString += ' --tag ' + registry + '/' + image + ':' + tag
        }

        withCredentials([usernamePassword(credentialsId: acctId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            sh 'buildah bud --format=docker --pull-always --no-cache --quiet --creds ${USERNAME}:${PASSWORD}' + tagString + ' .'
        }
    }
}

/**
 * Method to build and push a container image to the registry.
 * acctId - Identifier for the Jenkins Credential used to login to the registry.
 * registry -- The container registry host the image should be built for.
 * image -- The name of the image that should be build.
 * tags -- A list of tags associated with the specified image.
 **/
def buildAndPush(String acctId, String registry, String image, List<String> tags) {
    build(acctId, registry, image, tags)
    push(acctId, registry, image, tags)
}

/**
 * Method to build and push a container image to the registry.
 * acctId - Identifier for the Jenkins Credential used to login to the registry.
 * registry -- The container registry host the image should be built for.
 * product -- The name of the product the image is associated with.
 * module -- The name of the image.
 * tags -- A list of tags associated with the specified image.
 **/
def buildModule(String acctId, String registry, String product, String module, List<String> tags) {
    buildAndPush(acctId, registry, (ConfigurationDefaults.DEFAULT_IMAGE_BASE_CTX + "/" + product + "/" + module), tags)
}

/**
 * Method to build and push a container image to the registry.
 * acctId - Identifier for the Jenkins Credential used to login to the registry.
 * registry -- The container registry host the image should be built for.
 * product -- The name of the product the image is associated with.
 * modules -- The names of the images to build.
 * tags -- A list of tags associated with the specified image.
 **/
def buildModules(String acctId, String registry, String product, List<String> modules, List<String> tags) {
    for (mod in modules) { // Drop into each module directory and build.
        dir(mod) {
            buildAndPush(acctId, registry, (ConfigurationDefaults.DEFAULT_IMAGE_BASE_CTX + "/" + product + "/" + mod), tags)
        }
    }
}

return this
