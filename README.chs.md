# jenkins-pipeline-generator

旨在用简单的方法生成jenkins pipeline脚本。

## 介绍

该项目将生成您的应用程序jenkins管道脚本。

它将帮助您在jenkins上创建应用程序CD工作流。

只做一些配置。它会运行的很好。

## 快速开始

1. 安装Python版本超过2.X;

2. 通过pip安装flask. 

    ```shell script
    pip install flask
    ```

3. 运行.

    ```shell script
    python startup.py
    ```

4. 请求demo.

    [MUST] 必须检查的配置
    
    [NEED] 建议检查的配置
    
    [OPTION] 不重要的配置
    
    ```text
    {
        # [MUST]
        "namespace": "demo",
        # [MUST]
        "project_name": "share",
        "jenkins_properties": {
            # [MUST]
            "git_url": "git@github.com:gnosis-xian/jenkins-pipeline-generator.git",
            # [MUST]
            "branch": "master",
            # [MUST] Jenkins服务器上mvn命令的位置
            "maven_home": "/home/gnosis/apache-maven-3.6.1/bin/mvn",
            # [MUST] Jenkins服务器上maven配置文件的位置
            "maven_settings_file_path": "/home/gnosis/apache-maven-3.6.1/conf/yto/settings_yto_new.xml",
            # [MUST] Java可执行命令在目标部署服务器上的路径
            "java_home": "/usr/java/jdk_8u231/bin/java",
            # [MUST] 想要部署的目标服务器，list[0]为目标服务器IP，list[1]为目标服务器port，可以添加多台服务器
            "target_hosts": [
                ["192.168.207.49", "22"],
                ["192.168.207.49", "22"]
            ],
            # [MUST] 应用名
            "app_name": "share-app",
            # [MUST] 应用环境
            "env": "uat",
            # [NEED] 目标服务器应用home路径
            "app_home": "/root",
            # [MUST] 应用类型，这是由maven配置决定的
            "type": "application",
            # [MUST] 应用版本，这也是有maven配置决定的
            "project_version": "1.0.0-SNAPSHOT",
            # [NEED] 部署目标服务器用户名
            "host_user": "root",
            # [MUST] 认证ID，需要在Jenkins中配置git账号，并将ID copy到此处
            "git_credentials_id": "gaojing-yto-gitlab",
            # [OPTION] 多主机应用部署时时间间隔。
            "deploy_sleep_seconds": 0,
            # [OPTION] 是否备份旧版本软件包？
            "is_backup": false,
            # [OPTION] 是否使用当前分支打tag？
            "to_tag": false,
            # [OPTION] 是否进行代码静态检查？【暂未实现】
            "code_static_check": false,
            # [OPTION] 是否进行单元测试？【暂未实现】
            "unit_test": false,
            # [OPTION] 是否使用maven打包项目
            "maven_package": true
        }
    }
    ```

5. 使用响应信息在Jenkins中创建一个新项目

    响应:
    
    ```json
    {
      "Repository URL": "https://github.com/gnosis-xian/jenkins-pipeline-scripts.git",
      "Branch Specifier": "*/master",
      "Script Path": "demo/share-uat/share-app.groovy"
    }
    ```
   
   5.1 创建一个pipeline项目.
   
   ![](imgs/create-jenkins-project-1.png)
   
   5.2 配置.
   
   ![](imgs/create-jenkins-project-2.png)
   
   5.3 结束配置并构建.
   
   ![](imgs/create-jenkins-project-3.png)

## FQA

### 1. 创建Jenkins项目是没有pipeline选项

使用 **Manage Plugins** 安装名叫 **Pipeline** 的插件.

### 2. 不能从git上拉取代码.

- 检查 **git_credentials_id** 参数. 并保证账号在 **Credentials** 正确配置.

- 检查 git_url 是否正确.

## Contributors.

[gnosis-xian](https://github.com/gnosis-xian)