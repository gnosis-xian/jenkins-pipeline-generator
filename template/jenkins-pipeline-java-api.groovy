/**
 * Common parameters.
 */
// Parameters with http request input.
git_url = ${{git_url}}
maven_home = ${{maven_home}}
maven_settings_file_path = ${{maven_settings_file_path}}
java_home = ${{java_home}}
java_opt = ${{java_opt}}
target_hosts = ${{target_hosts}}
app_name = ${{app_name}}
env = ${{env}}
app_home = ${{app_home}}
branch = ${{branch}}
commit_id = ${{commit_id}}
type = ${{type}}
project_version = ${{project_version}}
host_user = ${{host_user}}
host_user_home_loc = get_host_user_home_loc()
git_credentials_id = ${{git_credentials_id}}
deploy_sleep_seconds = ${{deploy_sleep_seconds}}
sleep_seconds_after_kill = ${{sleep_seconds_after_kill}}
is_backup = ${{is_backup}}
to_tag = ${{to_tag}}
code_static_check = ${{code_static_check}}
unit_test = ${{unit_test}}
maven_package = ${{maven_package}}
maven_install = ${{maven_install}}
to_deploy = ${{to_deploy}}
deploy_stopping = ${{deploy_stopping}}
deploy_stopping_timeout_seconds = ${{deploy_stopping_timeout_seconds}}
increment = ${{increment}}

// Can be changed parameters.
detect_period_seconds = 15

// Can not be changed parameters.
deployed_count = 0
current_commit_id = ""
now_time = ""
jar_location = ""
jar_dir = ""


/**
 * Docker parameters.
 */
// Parameters with http request input.
with_docker = ${{with_docker}}
dockerfile_project_dockerfile_name = ${{dockerfile_project_dockerfile_name}}
dockerfile_project_git_url = ${{dockerfile_project_git_url}}
dockerfile_project_name = get_dockerfile_project_name(dockerfile_project_git_url)
elk_topic = ${{elk_topic}}
elk_kafka_cluster_list = ${{elk_kafka_cluster_list}}
docker_repo = ${{docker_repo}}
app_port = ${{app_port}}
docker_other_params = ${{docker_other_params}}

// Can be changed parameters.
setup_docker_resource_shell_name = "setup_docker_resource.sh"

// Can not be changed parameters.
dockerfile_project_home = ""
image_name = ""


node {
    try {
        if (hasCommitId()) {
           stage("Pull Code with commitId $commit_id") {
               delete_temp_branch()
               sh "git checkout $commit_id -b tmp__branch____"
               echo "Pulled $git_url commit_id: $commit_id ."
               doSomethingAfterPullFromGit()
           }
        } else {
            stage("Pull Code with branch $branch") {
                git branch: "$branch", credentialsId: "$git_credentials_id", url: "$git_url"
                echo "Pulled $git_url branch: $branch ."
                doSomethingAfterPullFromGit()
            }
        }

        if (to_tag) {
            stage("Tag to Git") {
                now_time = get_now_time()
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
            now_time = get_now_time()
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

        if (with_docker && !maven_install) {
            stage('Make docker image') {
                workspace_dir = sh returnStdout: true, script: "ls $WORKSPACE"
                docker_image_tag_name = "$env-$project_version-$now_time-$current_commit_id"
                image_name = "$docker_repo/$app_name:$docker_image_tag_name"
                if (!workspace_dir.contains("Dockerfile")) {
                    echo "Dockerfile not exist. Generate Dockerfile."
                    sh "cd $WORKSPACE && git clone $dockerfile_project_git_url"
                    current_project_dockerfile = "$dockerfile_project_home/dockerfiles/springboot-share-" + app_name + ".dockerfile"
                    if (java_opt == "") {
                        java_opt = "\"\""
                    }
                    generate_docker_file_command = "cat $dockerfile_project_home/dockerfiles/$dockerfile_project_dockerfile_name | \\\n" +
                            "sed \"s/{{APP_NAME}}/" + escape_char(app_name) + "/g\" | \\\n" +
                            "sed \"s/{{APP_PARAMS}}/" + escape_char("--spring.profiles.active=$env") + "/g\" | \\\n" +
                            "sed \"s/{{FILEBEATS_TOPIC}}/" + escape_char(elk_topic) + "/g\" | \\\n" +
                            "sed \"s/{{FILEBEAT_KAFKA_CLUSTER}}/" + escape_char(elk_kafka_cluster_list) + "/g\" | \\\n" +
                            "sed \"s/{{JAR_LOC}}/" + escape_char("./" + get_jar_relative_dir() + "/" + get_jar_name()) + "/g\" | \\\n" +
                            "sed \"s/{{JVM_PARAMS}}/" + escape_char(java_opt) + "/g\" \\\n" +
                            "> $current_project_dockerfile"
                    sh "cd $dockerfile_project_home && $generate_docker_file_command"
                    sh "cd $WORKSPACE && sudo docker build -f $current_project_dockerfile -t $image_name ."
                } else {
                    echo "Dockerfile exist. Use Dockerfile"
                    generate_docker_file_command = "cat $WORKSPACE/Dockerfile | \\\n" +
                            "sed \"s/{{APP_NAME}}/" + escape_char(app_name) + "/g\" | \\\n" +
                            "sed \"s/{{APP_PARAMS}}/" + escape_char("--spring.profiles.active=$env") + "/g\" | \\\n" +
                            "sed \"s/{{FILEBEATS_TOPIC}}/" + escape_char(elk_topic) + "/g\" | \\\n" +
                            "sed \"s/{{FILEBEAT_KAFKA_CLUSTER}}/" + escape_char(elk_kafka_cluster_list) + "/g\" | \\\n" +
                            "sed \"s/{{JAR_LOC}}/" + escape_char("./" + get_jar_relative_dir() + "/" + get_jar_name()) + "/g\" | \\\n" +
                            "sed \"s/{{JVM_PARAMS}}/" + escape_char(java_opt) + "/g\" \\\n" +
                            "> $WORKSPACE/Dockerfile_temp"
                    sh "cd $WORKSPACE && $generate_docker_file_command"
                    sh "cd $WORKSPACE && sudo docker build -f $WORKSPACE/Dockerfile_temp -t $image_name ."
                }
                sh "sudo docker push $image_name"
            }
        }

        if (deploy_stopping && !maven_install) {
            dir_pos = "/root/"
            next_ls_seconds = 10
            to_deploy_file_name = "goto_deploy"
            stage("Wait while [$to_deploy_file_name] file exist in $dir_pos.") {
                while (true) {
                    dir_content = sh returnStdout: true, script: "ls $dir_pos"
                    if (dir_content.contains(to_deploy_file_name)) {
                        break;
                    }
                    if (deploy_stopping_timeout_seconds <= 0) {
                        throw new Exception("Deploy stopping stage timeout. Increase deploy_stopping_timeout_seconds param and re-run this job.")
                    }
                    echo "Can not find [$to_deploy_file_name] file at $dir_pos. Wait while it exist. Next ls will after $next_ls_seconds seconds."
                    sleep(next_ls_seconds)
                    deploy_stopping_timeout_seconds = deploy_stopping_timeout_seconds - next_ls_seconds
                }
            }
        }

        if (to_deploy) {
            target_hosts.each { e ->
                host = e[0]
                port = e[1]
                /**
                 * Make dictionary for application.
                 */
                mkdirForProject(host, port)
                if (with_docker) {
                    stage("Deploy to $host with docker") {
                        echo "Begin to deploy $app_name to $host:$port with docker..."
                        /**
                         * Check docker component is right working on target host.
                         */
                        checkOrInstallDockerOnTargetHost(host, port)
                        containerName = "$app_name-$env"
                        /**
                         * Stop and remove container on target host.
                         */
                        stopDockerContainerAtTargetHost(host, port, containerName)
                        /**
                         * Startup application via docker.
                         */
                        startupDockerContainerAtTargetHost(host, port, containerName)
                        cleanAtTargetHost(host, port)
                        echo "Deploy to $host with docker has finished."
                    }
                } else {
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
                        if (++deployed_count != target_hosts.size()) {
                            echo "Wait $deploy_sleep_seconds and deploy application to next host."
                            sleep(deploy_sleep_seconds)
                        }
                    }
                }
            }
        }

        stage("Clean after finished") {
            clean_after_finished()
        }

    } catch (Exception exp) {
        clean_after_finished()
        throw exp
    }
}

def clean_after_finished() {
    delete_temp_branch()
    delete_workspace()
    delete_docker_temp()
}

def delete_docker_temp() {
    try {
        sh "sudo docker rmi $image_name"
    } catch (Exception exp) {
        echo "Delete docker image failed. Not exist."
    }
}

def delete_temp_branch() {
    try {
        sh "git checkout master"
        sh "git branch -D tmp__branch____"
    } catch (Exception exp) {
        echo "Delete temp branch throw exception"
    }
}

def delete_workspace() {
    sh "cd $WORKSPACE && rm -rf $dockerfile_project_name"
}

def hasCommitId() {
    if (commit_id == "") {
        return false;
    }
    return true;
}

def copyProjectFileToTargetHosts(host, port) {
    scp_command = "scp -o StrictHostKeyChecking=no -P $port $jar_location $host_user@$host:$app_home/$app_name" + ".jar"
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

    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'echo \"sudo kill -15 \\\$(ps aux | grep java | grep '$app_name' | grep '$env' | awk \\\"{print \\\\\\\$2}\\\")\" > '$app_home'/shutdown.sh\'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'echo \"sudo kill -9 \\\$(ps aux | grep java | grep '$app_name' | grep '$env' | awk \\\"{print \\\\\\\$2}\\\")\" > '$app_home'/shutdown_force.sh\'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'echo \"ps aux | grep java | grep '$app_name' | grep '$env' | awk \\\"{print \\\\\\\$2}\\\"\" > '$app_home'/detect.sh\'"

    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'chmod +x '$app_home'/shutdown.sh\'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'chmod +x '$app_home'/shutdown_force.sh\'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'chmod +x '$app_home'/detect.sh\'"

    shutdown_app(host_user, host, port, app_home)

    detect_times = 0
    while (true) {
        detect_times = detect_times + 1
        detect_result = sh returnStdout: true, script: "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \\''$app_home'/detect.sh\\'"
        if (detect_result == '') {
            echo "Detect $app_name at $host:$port was shutdown."
            break;
        }
        detect_result = detect_result.replace("\n", ",")
        echo "Detect[Times: $detect_times] $app_name at $host:$port still running ($detect_result) "
        if (detect_times > 3 && detect_times <= 6) {
            shutdown_app_force(host_user, host, port, app_home)
        }
        if (detect_times > 6) {
            notice_deploy_fail(host_user, host, port, app_home, app_name)
            break;
        }
        sleep(detect_period_seconds)
    }
    echo "$detect_result"
    echo "Sleep $sleep_seconds_after_kill seconds after kill application process."
    sleep(sleep_seconds_after_kill)
}

def shutdown_app(host_user, host, port, app_home) {
    try {
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \''$app_home'/shutdown.sh\'"
    } catch (Exception ignored) {
    }
}

def shutdown_app_force(host_user, host, port, app_home) {
    echo "Force to shutdown $app_name at $host:$port"
    try {
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \''$app_home'/shutdown_force.sh\'"
    } catch (Exception ignored) {
    }
}

def notice_deploy_fail(host_user, host, port, app_home, app_name) {
    msg = "[WARNING] $app_name at $host_user@$host:$port from $app_home deploy failed. Please check status or process manually."
    echo "$msg"
}

def startupProjectProcessAtTargetHost(host, port) {
    echo "Startup $app_name on $host:$port"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'echo \" sudo nohup '$java_home' $java_opt -jar '$app_home'/'$app_name'.jar --git.commit.id='$current_commit_id' --host.info.ip='$host' --host.info.port='$port' --spring.profiles.active='$env' --spring.shardingsphere.sharding.default-key-generator.props.worker.id='$increment' > /dev/null 2>&1 & \" > '$app_home'/startup.sh \'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'chmod +x '$app_home'/startup.sh\'"
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \''$app_home'/startup.sh\'"
    increment = increment + 1
}

def doSomethingAfterPullFromGit() {
    current_commit_id = sh returnStdout: true, script: "echo -n `git rev-parse HEAD`"
    dockerfile_project_home="$WORKSPACE/$dockerfile_project_name"
    jar_location = get_jar_location()
    jar_dir = get_jar_dir()
    now_time = get_now_time()
}

def get_dockerfile_project_name(String dockerfile_project_git_url) {
    if (dockerfile_project_git_url == '') {
        return ''
    }
    return dockerfile_project_git_url.substring(dockerfile_project_git_url.lastIndexOf('/') + 1, dockerfile_project_git_url.length() - 4)
}

def get_host_user_home_loc() {
    if ("root".equals(host_user)) {
        return "/$host_user"
    } else {
        return "/home/$host_user"
    }
}

def get_now_time() {
    result = ""
    if ("".equals(now_time)) {
        result = sh returnStdout: true, script: "echo -n `date +%Y%m%d%H%M%S`"
        return result
    }
    return now_time
}

def escape_char(String str) {
    return str.replace("\"", "\\\"")
            .replace("/", "\\/")
}

def get_jar_location() {
    return get_jar_dir() + "/" + get_jar_name()
}

def get_jar_dir() {
    return "$WORKSPACE/$app_name-$type/target"
}

def get_jar_relative_dir() {
    return "$app_name-$type/target"
}

def get_jar_name() {
    return "$app_name-$type-$project_version" + ".jar"
}

def stopDockerContainerAtTargetHost(host, port, containerName) {
    try {
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sudo docker stop $containerName"
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sudo docker rm -f $containerName"
    } catch (Exception e) {
        echo "Container [$containerName] not running on $host:$port"
        try {
            sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sudo docker rm -f $containerName"
        } catch (Exception ex) {
            echo "Container [$containerName] not exist on $host:$port"
        }
    }
}

def startupDockerContainerAtTargetHost(host, port, containerName) {
    jvm_param = java_opt
    app_param = "--spring.profiles.active=$env --server.port=$app_port --spring.cloud.nacos.discovery.ip=$host --git.commit.id=$current_commit_id --host.info.ip=$host --host.info.port=$port --spring.shardingsphere.sharding.default-key-generator.props.worker.id=$increment"
    docker_startup_command = "sudo docker run -d --name $containerName \\\n" +
            "-e JVM_PARAMS=\\\"$jvm_param\\\" \\\n" +
            "-e APP_PARAMS=\\\"$app_param\\\" \\\n" +
            "-p $app_port:$app_port \\\n" +
            "$docker_other_params \\\n" +
            "$image_name"
    try {
        docker_startup_filename = "startup_docker.sh"
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'echo \"$docker_startup_command\" > '$app_home'/$docker_startup_filename \'"
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sh $app_home/$docker_startup_filename"
    } catch (Exception e) {
        echo "Container [$containerName] startup fail on $host:$port. Kill process and try again."
        killProjectProcessAtTargetHost(host, port)
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \'echo \"$docker_startup_command\" > '$app_home'/$docker_startup_filename \'"
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sh $app_home/$docker_startup_filename"
    }
    increment = increment + 1
}

def checkOrInstallDockerOnTargetHost(host, port) {
    try {
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sudo systemctl start docker"
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sudo docker ps -a"
    } catch (Exception e) {
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sudo yum install -y docker"
        sh "scp -o StrictHostKeyChecking=no -P $port $dockerfile_project_home/dockerfiles/setup_docker_resource.sh $host_user@$host:$host_user_home_loc/"
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port sh $host_user_home_loc/$setup_docker_resource_shell_name"
    }
}

def mkdirForProject(host, port) {
    sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port mkdir -p $app_home"
}

def cleanAtTargetHost(host, port) {
    try {
        sh "ssh -o StrictHostKeyChecking=no $host_user@$host -p $port \"sudo docker images | grep $app_name | grep -v $image_name | awk '{print \$3}' | xargs docker rmi -f\""
    } catch (Exception e) {
        echo "Delete useless docker images on $host:$port failed.."
    }
}