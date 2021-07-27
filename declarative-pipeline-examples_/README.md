Jenkins Declarative Pipeline Documentation
=====================================

Job Creation 
=====================================
An excellent article on the process of creating new Bitbucket Team/Project type Jenkins jobs can be found [here](https://support.cloudbees.com/hc/en-us/articles/115000053051-How-to-Trigger-Multibranch-Jobs-from-Bitbucket-Server-). Please read the relevant section for our BitBucket server version prior to continuing here.

At a high level the process works as follows:

1) Login to the appropriate Jenkins instance for your team's product portfolio. 
2) Click the New Item button on the left navigation pane. You will be presented with a list of options for the new item you're creating and asked to give it a name. Select "Bitbucket Team/Project" and provide a name. We recommend using your product's name or the name of your Bitbucket project. (Names should match the regex [A-Za-z]. You may set an unconstrained display label later. Click ok.
3) You will see a configuration screen. You will ***only*** need to configure the credentials, owner, scan organization folder triggers, and scan child triggers fields. You will need to scroll to find them all. After you've configured those elements click save. You will be taken to a screen showing Jenkins scanning your project. Jenkins will create folders for all repos that meet the configured criteria and within those folders jobs for each branch that has a Jenkinsfile at its root.
	1) Bitbucket Team/Project / Credentials - Set the field to the credential with the description "Jenkins Service Account Username/Password". These are the credentials Jenkins will use to communicate with Bitbucket.
    2) Bitbucket Team/Project / Owner - Set the field to the project key for your Bitbucket project. The owner is the project Jenkins will monitor in Bitbucket.
    3) Scan Organization Folder Triggers / Interval - Set the interval field to "1 Hour". The scan interval is the frequency at which Jenkins will re-scan your Bitbucket project to check for new repositories. You may scan less frequently if you like.
    4) Scan Child Triggers / Interval - Set the interval field to "30 Minutes". The scan interval is the frequency at which Jenkins will re-scan your repos to check for new changes. You may scan less frequently if you like.
