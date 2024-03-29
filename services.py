from pipeline_scripts_generator import *
from git_utils import *

def web_service(namespace, project_name, scm_url, jenkins_propertie, docker_info):

    git_url = jenkins_propertie['git_url']
    npm_home = jenkins_propertie['npm_home']
    yarn_home = jenkins_propertie['yarn_home']
    target_hosts = jenkins_propertie['target_hosts']
    app_name = jenkins_propertie['app_name']
    env = jenkins_propertie['env']
    app_home = jenkins_propertie['app_home']
    branch = jenkins_propertie['branch']
    npm_registry_url = jenkins_propertie['npm_registry_url']
    host_user = jenkins_propertie['host_user']
    git_credentials_id = jenkins_propertie['git_credentials_id']
    package_type = jenkins_propertie['package_type']
    is_backup = jenkins_propertie['is_backup']
    deploy_sleep_seconds = jenkins_propertie['deploy_sleep_seconds']
    to_tag = jenkins_propertie['to_tag']
    to_compile = jenkins_propertie['to_compile']
    build_param = jenkins_propertie['build_param']

    jenkins_content = generate_pipeline(
        pro_type='web',
        git_url=git_url,
        npm_home=npm_home,
        yarn_home=yarn_home,
        target_hosts=target_hosts,
        app_name=app_name,
        env=env,
        app_home=app_home,
        branch=branch,
        npm_registry_url=npm_registry_url,
        host_user=host_user,
        git_credentials_id=git_credentials_id,
        package_type=package_type,
        is_backup=is_backup,
        deploy_sleep_seconds=deploy_sleep_seconds,
        to_tag=to_tag,
        to_compile=to_compile,
        build_param=build_param,
        docker_info=docker_info
    )

    jenkins_pipeline_path = write_content_to_file(scm_url, namespace, "web", project_name, app_name, env, jenkins_content)

    file_path = "{}/{}-{}/web-{}.groovy".format(namespace, project_name, env, app_name)

    git_actions(scm_url, file_path, project_name, app_name, env)

    return file_path

def java_api_service(java_type, namespace, project_name, scm_url, jenkins_propertie, docker_info):
    git_url = get_value_safty(jenkins_propertie, 'git_url')
    maven_home = get_value_safty(jenkins_propertie, 'maven_home')
    maven_settings_file_path = get_value_safty(jenkins_propertie, 'maven_settings_file_path')
    java_home = get_value_safty(jenkins_propertie, 'java_home')
    java_opt = get_value_safty(jenkins_propertie, 'java_opt')
    target_hosts = get_value_safty(jenkins_propertie, 'target_hosts')
    app_name = get_value_safty(jenkins_propertie, 'app_name')
    project_child_path = get_value_safty(jenkins_propertie, 'project_child_path')
    package_jar_location = get_value_safty(jenkins_propertie, 'package_jar_location')
    env = get_value_safty(jenkins_propertie, 'env')
    app_home = get_value_safty(jenkins_propertie, 'app_home')
    branch = get_value_safty(jenkins_propertie, 'branch')
    commit_id = get_value_safty(jenkins_propertie, 'commit_id')
    type = get_value_safty(jenkins_propertie, 'type')
    project_version = get_value_safty(jenkins_propertie, 'project_version')
    host_user = get_value_safty(jenkins_propertie, 'host_user')
    git_credentials_id = get_value_safty(jenkins_propertie, 'git_credentials_id')
    deploy_sleep_seconds = get_value_safty(jenkins_propertie, 'deploy_sleep_seconds')
    sleep_seconds_after_kill = get_value_safty(jenkins_propertie, 'sleep_seconds_after_kill')
    is_backup = get_value_safty(jenkins_propertie, 'is_backup')
    to_tag = get_value_safty(jenkins_propertie, 'to_tag')
    code_static_check = get_value_safty(jenkins_propertie, 'code_static_check')
    unit_test = get_value_safty(jenkins_propertie, 'unit_test')
    maven_package = get_value_safty(jenkins_propertie, 'maven_package')
    maven_install = get_value_safty(jenkins_propertie, 'maven_install')
    to_deploy = get_value_safty(jenkins_propertie, 'to_deploy')
    deploy_stopping = get_value_safty(jenkins_propertie, 'deploy_stopping')
    deploy_stopping_timeout_seconds = get_value_safty(jenkins_propertie, 'deploy_stopping_timeout_seconds')
    increment = get_value_safty(jenkins_propertie, 'increment')
    copy_jar_to_hosts = get_value_safty(jenkins_propertie, 'copy_jar_to_hosts')
    copy_hosts = get_value_safty(jenkins_propertie, 'copy_hosts')
    copy_host_user = get_value_safty(jenkins_propertie, 'copy_host_user')
    copy_host_dir = get_value_safty(jenkins_propertie, 'copy_host_dir')
    app_port = get_value_safty(jenkins_propertie, 'app_port')
    docker_other_params = get_value_safty(jenkins_propertie, 'docker_other_params')

    jenkins_content = generate_pipeline(
        pro_type=java_type,
        git_url=git_url,
        maven_home=maven_home,
        maven_settings_file_path=maven_settings_file_path,
        java_home=java_home,
        java_opt=java_opt,
        target_hosts=target_hosts,
        app_name=app_name,
        project_child_path=project_child_path,
        package_jar_location=package_jar_location,
        env=env,
        app_home=app_home,
        branch=branch,
        commit_id=commit_id,
        type=type,
        project_version=project_version,
        host_user=host_user,
        git_credentials_id=git_credentials_id,
        deploy_sleep_seconds=deploy_sleep_seconds,
        sleep_seconds_after_kill=sleep_seconds_after_kill,
        is_backup=is_backup,
        to_tag=to_tag,
        code_static_check=code_static_check,
        unit_test=unit_test,
        maven_package=maven_package,
        maven_install=maven_install,
        to_deploy=to_deploy,
        deploy_stopping=deploy_stopping,
        deploy_stopping_timeout_seconds=deploy_stopping_timeout_seconds,
        increment=increment,
        copy_jar_to_hosts=copy_jar_to_hosts,
        copy_hosts=copy_hosts,
        copy_host_user=copy_host_user,
        copy_host_dir=copy_host_dir,
        app_port=app_port,
        docker_other_params=docker_other_params,
        docker_info=docker_info
    )

    jenkins_pipeline_path = write_content_to_file(scm_url, namespace, "api", project_name, app_name, env, jenkins_content)

    file_path = "{}/{}-{}/api-{}.groovy".format(namespace, project_name, env, app_name)

    git_actions(scm_url, file_path, project_name, app_name, env)

    return file_path