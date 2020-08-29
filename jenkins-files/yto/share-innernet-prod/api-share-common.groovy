git_url = "git@10.1.193.100:YTO-eCommerce/ec-commerce/share/share-common.git"
maven_home = "/home/yjt/apache-maven-3.6.3/bin/mvn"
maven_settings_file_path = "/home/yjt/apache-maven-3.6.3/conf/yto/settings_yto_new_share.xml"
java_home = ""
java_opt = ""
target_hosts = ""
app_name = "share-common"
env = "prod"
app_home = "/$app_name-$env"
branch = "zto_bug_fix_829"
commit_id = ""
type = ""
project_version = ""
host_user = ""
git_credentials_id = "gaojing-yto-gitlab"
deploy_sleep_seconds = 
sleep_seconds_after_kill = 
is_backup = false
to_tag = true
code_static_check = false
unit_test = false
maven_package = false
maven_install = true
to_deploy = false

node {
    if (hasCommitId()) {
       stage("Pull Code with commitId $commit_id") {
           delete_temp_branch()
           sh "git checkout $commit_id -b tmp__branch____"
           echo "Pulled $git_url commit_id: $commit_id ."
       }
    } else {
        stage("Pull Code with branch $branch") {
            git branch: "$branch", credentialsId: "$git_credentials_id", url: "$git_url"
            echo "Pulled $git_url branch: $branch ."
        }
    }

    if (to_tag) {
        stage("Tag to Git") {
            now_time = sh returnStdout: true, script: "echo -n `date +%Y%m%d%H%M%S`"
            tag_name = ''
            if (hasCommitId()) {
                tag_name = "tag_from_commitId_" + commit_id + "_for_" + env + "_at_" + now_time
            } else {
                tag_name = "tag_from_branch_" + branch + "_for_" + env + "_at_" + now_time
            }
            sh "git tag $tag_name"
            sh "git push origin $tag_name"
            echo "Tag $tag_name has pushed to remote."
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
                try {
                    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port cp $jar_path $baked_jar_path"
                    echo "Origin jar were backuped on $host:$baked_jar_path"
                } catch (Exception ignored) {
                    echo "Origin jar not exist. Ignored backup..."
                }
            }
        }
    }

    if (maven_install) {
        stage('Maven Install') {
            sh "$maven_home clean install -DskipTest=true  --settings=$maven_settings_file_path"
            echo "Maven Clean and Install $app_name:$branch"
        }
    }

    if (maven_package) {
        stage('Compile') {
            sh "$maven_home clean package -Dmaven.test.skip=true --settings=$maven_settings_file_path -P $env"
            echo 'Compile without test cases finished.'
        }
    }

    if (to_deploy) {
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

    stage("Clean after finished") {
        clean_after_finished()
    }
}

def clean_after_finished() {
    delete_temp_branch()
}

def delete_temp_branch() {
    try {
        sh "git checkout master"
        sh "git branch -D tmp__branch____"
    } catch (Exception exp) {
        echo "Delete temp branch throw exception"
    }
}

def hasCommitId() {
    if (commit_id == "") {
        return false;
    }
    return true;
}

def copyProjectFileToTargetHosts(host, port) {
    scp_command = "scp -o StrictHostKeyChecking=no -P $port $WORKSPACE/$app_name-$type/target/$app_name-$type-$project_version" + ".jar" + " $host_user@$host:$app_home/$app_name" + ".jar"
    echo "Copy $app_name $host:$port:$app_home. Command: $scp_command"
    try {
        sh "$scp_command"
    } catch (Exception exp) {
        echo "Project dictionary were not created. Create dictionary on $host:$port:$app_home automatically."
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port mkdir -p $app_home"
        sh "$scp_command"
    }
}

def killProjectProcessAtTargetHost(host, port) {
    echo "Kill $app_name process on $host:$port"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'echo \"sudo kill -15 \\\$(ps -aux | grep java | grep '$app_name' | grep '$env' | awk \\\"{print \\\\\\\$2}\\\")\" > '$app_home'/shutdown.sh\'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'chmod +x '$app_home'/shutdown.sh\'"
    try {
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \''$app_home'/shutdown.sh\'"
    } catch (Exception ignored) {
    }

    echo "Sleep $sleep_seconds_after_kill seconds after kill application process."
    sleep(sleep_seconds_after_kill)
}

def startupProjectProcessAtTargetHost(host, port) {
    echo "Startup $app_name on $host:$port"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'echo \" sudo nohup '$java_home' $java_opt -jar '$app_home'/'$app_name'.jar --spring.profiles.active='$env' > /dev/null 2>&1 & \" > '$app_home'/startup.sh \'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'chmod +x '$app_home'/startup.sh\'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \''$app_home'/startup.sh\'"
}