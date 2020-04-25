git_url = "git@github.com:gnosis-xian/jenkins-pipeline-generator.git"
npm_home = "/usr/bin/npm"
yarn_home = "/usr/local/yarn-v1.22.4/bin/yarn"
target_hosts = [["192.168.207.49", "22"], ["192.168.207.49", "22"]]
app_name = "share-app"
env = "dev"
app_home = "/root/$app_name-$env"
branch = "master"
npm_registry_url = ""
host_user = "root"
git_credentials_id = "gaojing-yto-gitlab"
package_type = "npm"
is_backup = false
deploy_sleep_seconds = 0
to_tag = false
to_compile = true

node {
    stage('Registry Settings') {
        default_registry = "https://registry.npm.taobao.org/"
        try {
            if (npm_registry_url == "") {
                echo "Registry URL is: $default_registry"
                sh "npm config set registry $default_registry"
            } else {
                echo "Registry URL is: $npm_registry_url"
                sh "npm config set registry $npm_registry_url"
            }
        } catch (Exception ignored) {
        }
    }

    stage('Pull Code') {
        git branch: "$branch", credentialsId: "$git_credentials_id", url: "$git_url"
        echo "Pulled $url branch: $branch ."
    }

    if (to_tag) {
        stage("Tag to Git") {
            now_time = sh returnStdout: true, script: "echo -n `date +%Y%m%d%H%M`"
            tag_name = "tag_from_" + branch + "_for_" + env + "_at_" + now_time
            sh "git tag $tag_name"
            sh "git push origin $tag_name"
            echo "Tag $tag_name from $branch has pushed to remote."
        }
    }

    if (to_compile) {
        stage('Delete old dist') {
            dist_dir = "$WORKSPACE/dist"
            echo "Delete fore project **dist** dictionary. $dist_dir"
            sh "rm -rf $dist_dir"
        }

        stage('Compile') {
            echo "Start package with $package_type"
            switch (package_type) {
                case 'npm':
                    sh "$npm_home install"
                    sh "$npm_home run lint"
                    sh "$npm_home run build"
                    break;
                case 'yarn':
                    sh "$yarn_home install"
                    sh "$yarn_home build"
                    break;
            }
        }

        stage('ZIP WEB tar') {
            sh "cd $WORKSPACE/dist && chmod 777 * -R && tar -cvf app.tar *"
        }
    }

    target_hosts.each { e ->
        count = 0;
        host = e[0]
        port = e[1]

        stage("Deploy to $host") {
            echo "Begin to deploy $app_name to $host:$port ..."
            copyProjectFileToTargetHosts(host, port)
            if (is_backup) {
                backupWebTar(host, port)
            }
            unzipProjectFileOnTargetHost(host, port)
            reloadNginx(host, port)
            cleanWebTar(host, port)
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
    scp_command = "scp -o StrictHostKeyChecking=no -P $port '$WORKSPACE'/dist/app.tar $host_user@$host:$app_home/app.tar"
    try {
        sh "$scp_command"
    } catch (Exception ex) {
        echo "Project dictionary were not created. Create dictionary on $host:$port:$app_home automatically."
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port mkdir -p $app_home"
        sh "$scp_command"
    }
}

def unzipProjectFileOnTargetHost(host, port) {
    echo "Unzip WEB project tar on $host:$port"
    // sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port 'cd $app_home && rm -rf index.html static/'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port tar -xvf $app_home/app.tar -C $app_home"
}

def reloadNginx(host, port) {
    echo "Reload Nginx on $host:$port"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sudo /usr/sbin/nginx -s reload"
}

def cleanWebTar(host, port) {
    echo "Clean WEB tar from Jenkins server."
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port 'cd $app_home && rm -rf app.tar'"
}

def backupWebTar(host, port) {
    stage("Backup on $host") {
        now_time = sh returnStdout: true, script: "echo -n `date +%Y%m%d%H%M`"
        backup_dir = "$app_home/backups"
        scp_command = "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port cp $app_home/app.tar $backup_dir/app.tar.$now_time"
        try {
            sh "$scp_command"
        } catch (Exception ex) {
            echo "Project backup dictionary were not created. Create dictionary on $host:$port:$backup_dir automatically."
            sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port mkdir -p $backup_dir"
            sh "$scp_command"
        }
    }
}