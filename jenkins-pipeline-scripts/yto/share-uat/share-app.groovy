node {
    git_url = "git@10.1.193.100:YTO-eCommerce/ec-commerce/share/share-app-server.git"
    maven_home = "/home/gnosis/apache-maven-3.6.1/bin/mvn"
    maven_settings_file_path = "/home/gnosis/apache-maven-3.6.1/conf/yto/settings_yto_new.xml"
    java_home = "/usr/java/jdk_8u231/bin/java"
    target_hosts = [["192.168.207.49", "22"], ["192.168.207.49", "22"]]
    app_name = "share-app"
    env = "uat"
    app_home = "/root/$app_name-$env"
    branch = "master"
    type = "application"
    project_version = "1.0.0-SNAPSHOT"
    host_user = "root"
    git_credentials_id = "gaojing-yto-gitlab"
    deploy_sleep_seconds = 0
    is_backup = true
    to_tag = false
    code_static_check = false
    unit_test = false
    maven_package = true

    stage('Pull Code') {
        git branch: "$branch", credentialsId: "$git_credentials_id", url: "$git_url"
        echo "Pulled $git_url branch: $branch ."
    }

    if (to_tag) {
        stage("Tag to Git") {
            now_time = sh returnStdout: true, script: "echo -n `date +%Y%m%d%H%M%S`"
            tag_name = "tag_from_" + branch + "_for_" + env + "_at_" + now_time
            sh "git tag $tag_name"
            sh "git push origin $tag_name"
            echo "Tag $tag_name from $branch has pushed to remote."
        }
    }

    if (code_static_check) {
        stage('SonarQube analysis') {
            echo "NOT IMPLEMENT NOW..."
        }
    }

    if (unit_test) {
        stage('Unit Test') {
            echo "NOT IMPLEMENT NOW..."
        }
    }

    if (is_backup) {
        now_time = sh returnStdout: true, script: "echo -n `date +%Y%m%d%H%M%S`"
        target_hosts.each { e ->
            host = e[0]
            port = e[1]
            stage("Backup on $host") {
                jar_path = "$app_home/$app_name" + ".jar"
                baked_jar_path = "$jar_path.$now_time"
                sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port cp $jar_path $baked_jar_path"
                echo "Origin jar were backuped on $host:$baked_jar_path"
            }
        }
    }

    if (maven_package) {
        stage('Compile') {
            sh "$maven_home clean package -Dmaven.test.skip=true --settings=$maven_settings_file_path -P $env"
            echo 'Compile without test cases finished.'
        }
    }

    target_hosts.each { e ->
        count = 0;
        host = e[0]
        port = e[1]

        stage("Deploy to $host") {
            echo "Begin to deploy $app_name to $host:$port ..."

            /**
             * Copy project file to target hosts.
             */
            copyProjectFileToTargetHosts(host, port)

            /**
             * Shutdown project process on target hosts.
             */
            killProjectProcessAtTargetHost(host, port)

            /**
             * Startup project process on target hosts.
             */
            startupProjectProcessAtTargetHost(host, port)

            echo "Deploy to $host finished."

            /**
             * Wait will the previous project startup.
             */
            if (++count != target_hosts.size()) {
                echo "Wait $deploy_sleep_seconds and deploy application to next host."
                sleep(deploy_sleep_seconds)
            }
        }
    }
}

def copyProjectFileToTargetHosts(host, port) {
    scp_command = "scp -o StrictHostKeyChecking=no -P $port $WORKSPACE/$app_name-$type/target/$app_name-$type-$project_version" + ".jar" + " $host_user@$host:$app_home/$app_name" + ".jar"
    echo "Copy $app_name $host:$port:$app_home. Command: $scp_command"
    try {
        sh "$scp_command"
    } catch (Exception exp) {
        echo "Project dictionary were not created. Create dictionary on $host:$port:$app_home automatically."
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sudo mkdir -p $app_home"
        sh "$scp_command"
    }
}

def killProjectProcessAtTargetHost(host, port) {
    echo "Kill $app_name process on $host:$port"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'echo \"sudo kill -9 \\\$(ps -aux | grep java | grep '$app_name' | grep '$env' | awk \\\"{print \\\\\\\$2}\\\")\" > '$app_home'/shutdown.sh\'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'sudo chmod +x '$app_home'/shutdown.sh\'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \''$app_home'/shutdown.sh\'"
}

def startupProjectProcessAtTargetHost(host, port) {
    echo "Startup $app_name on %s:%s"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'echo \" sudo nohup '$java_home' -jar '$app_home'/'$app_name'.jar --spring.profiles.active='$env' > /dev/null 2>&1 & \" > '$app_home'/startup.sh \'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'sudo chmod +x '$app_home'/startup.sh\'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \''$app_home'/startup.sh\'"
}