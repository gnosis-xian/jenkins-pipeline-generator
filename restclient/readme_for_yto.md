```text
"app_name": "share-cockpit-api", // app名称，自己起，推荐和项目git名保持一致
"git_url": "git@github.com:a/b/c/share-cockpit-api.git", // app git url 推荐使用 ssh
"branch": "dev_20200616", // 分支名称
"java_home": "/opt/java/jdk1.8.0_191/bin/java", // 应用服务器 java 命令绝对路径
"java_opt": {{java_opt}}, // JVM参数，默认 -server -Xmx1024M -Xms1024M -XX:MaxMetaspaceSize=512M -XX:MetaspaceSize=512M -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/root/
"target_hosts": [["192.168.207.153", "22"]], // 应用服务器 ip 和 端口
"env": "test", // 应用发布环境 如 dev test uat... 和 application-XXX.properties 保持一致
"app_home": "/root/ytoapp", // 应用服务器发布根路径
"type": {{type_application}}, // 应用类型 参考pom.xml配置 如：application / controller
"project_version": {{project_version_1_0}}, // 应用版本 参考pom.xml配置 如：1.0-SNAPSHOT 
"host_user": {{host_user}}, // 应用服务器登录用户名
"is_backup": {{is_backup}}, // 是否需要备份上次发布的jar包
"to_tag": {{to_tag}}, // 是否需要自动打tag // 需要给git账号开通master权限
"maven_package": {{maven_package}}, // 是否需要package，jar包为true
"maven_install": {{maven_install}}, // 是否需要install，common工程为true
"to_deploy": {{to_deploy}} // 是否需要部署到指定应用服务器，默认为true
```
