#!/usr/bin/groovy
package com.ercot.build

/**
 * Method that returns all tags on the checked out git commit.
 **/
def List<String> getTags() {

    List<String> tags = new ArrayList<String>();

    String tmpTag = sh(returnStdout: true, script: 'git tag -l --points-at HEAD').trim() // Get all tags on the commit.
    if(tmpTag != null && !tmpTag.isEmpty()){
        String[] commitTags = tmpTag.split("\n")
        if(commitTags != null) {
            tags.addAll(java.util.Arrays.asList(commitTags))
        }
    }

    return tags;
}

/**
 * Method that returns all tags on the checked out commit as well as the git commit hash.
 **/
def List<String> getContainerTags() {

    List<String> tags = new ArrayList<String>();
    tags.addAll(getTags())
    tags.add(sh(returnStdout: true, script: 'git log -n1 --format=format:"%H"').trim()) // Get the long commit hash.

    return tags;
}

/**
 * Method that returns the branch name on the checked out repo.
 **/
def String getBranchName() {

    return sh(returnStdout: true, script: 'git branch --show-current').trim(); // Get the branch name.

}

return this
