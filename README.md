# jenkins-pipeline-generator

Aim to generate jenkins pipeline scripts in easy way.

## Introduction

This project will generate your application jenkins pipeline scripts.

It will help you to create the application CD workflow on jenkins.

Just some steps to configuration. It will be work will.

## Quick Start

1. Install Python version over 2.X;

2. Install flask via pip. 

    ```shell script
    pip install flask
    ```

3. Run.

    ```shell script
    python startup.py
    ```

4. Demo Request 

More detail request [click this](./restclient/rest-test.rest).

[MUST] Important check.

[NEED] Suggest check.

[OPTION] Check or not.
    
    ```http request
    POST http://gnosis.gq:5000/gen-jenkins-pipeline/java-api HTTP/1.1
    Content-Type: application/json
    
    {
        "namespace": "demo",
        "project_name": "share",
        "jenkins_properties": {
            "git_url": "git@github.com:gnosis-xian/jenkins-pipeline-generator.git",
            "maven_home": "/home/gnosis/apache-maven-3.6.1/bin/mvn",
            "maven_settings_file_path": "/home/gnosis/apache-maven-3.6.1/conf/yto/settings_yto_new.xml",
            "java_home": "/usr/java/jdk_8u231/bin/java",
            "target_hosts": [
                ["192.168.207.49", "22"],
                ["192.168.207.49", "22"]
            ],
            "app_name": "share-app",
            "env": "uat",
            "app_home": "/root",
            "branch": "master",
            "type": "application",
            "project_version": "1.0.0-SNAPSHOT",
            "host_user": "root",
            "git_credentials_id": "gaojing-yto-gitlab",
            "deploy_sleep_seconds": 0,
            "is_backup": true,
            "to_tag": false,
            "code_static_check": false,
            "unit_test": false,
            "maven_package": true
        }
    }
    ```

5. Create a new project in Jenkins use response.

    Response:
    
    ```json
    {
      "Repository URL": "https://github.com/gnosis-xian/jenkins-pipeline-scripts.git",
      "Branch Specifier": "*/master",
      "Script Path": "demo/share-uat/share-app.groovy"
    }
    ```
   
   5.1 New Jenkins pipeline project.
   
   ![](imgs/create-jenkins-project-1.png)
   
   5.2 Config.
   
   ![](imgs/create-jenkins-project-2.png)
   
   5.3 Finish and build.
   
   ![](imgs/create-jenkins-project-3.png)

## FQA

### 1. Pipeline selection not found when create jenkins project.

Install pipeline plugin named **Pipeline** with Jenkins **Manage Plugins**.

### 2. Can't pull with git.

Check **git_credentials_id** parameter. And it has config in Jenkins **Credentials**.

## Contributors.

[gnosis-xian](https://github.com/gnosis-xian)