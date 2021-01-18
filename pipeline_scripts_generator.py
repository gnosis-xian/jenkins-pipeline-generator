import json
import os
from constants import *
from git_utils import *

def read_file_content(file_path):
    file = open(file_path, "r")
    return file.read()

def write_file(file_path, write_content):
    with open(file_path, "w") as file:
        file.write(write_content)

def get_value_safty(params, key):
    result = ''
    try:
        result = params[key]
    except Exception:
        return result
    return result

def generate_pipeline(**params):
    file_content = read_file_content("template/jenkins-pipeline-{}.groovy".format(get_value_safty(params, 'pro_type')))
    file_content = file_content.replace("${{git_url}}", json.dumps(get_value_safty(params, 'git_url')))
    file_content = file_content.replace("${{maven_home}}", json.dumps(get_value_safty(params, 'maven_home')))
    file_content = file_content.replace("${{maven_settings_file_path}}", json.dumps(get_value_safty(params, 'maven_settings_file_path')))
    file_content = file_content.replace("${{java_home}}", json.dumps(get_value_safty(params, 'java_home')))
    file_content = file_content.replace("${{java_opt}}", json.dumps(get_value_safty(params, 'java_opt')))
    file_content = file_content.replace("${{target_hosts}}", json.dumps(get_value_safty(params, 'target_hosts')))
    file_content = file_content.replace("${{app_name}}", json.dumps(get_value_safty(params, 'app_name')))
    file_content = file_content.replace("${{project_child_path}}", json.dumps(get_value_safty(params, 'project_child_path')))
    file_content = file_content.replace("${{env}}", json.dumps(get_value_safty(params, 'env')))
    file_content = file_content.replace("${{app_home}}", json.dumps(get_value_safty(params, 'app_home') + "/$app_name-$env"))
    file_content = file_content.replace("${{branch}}", json.dumps(get_value_safty(params, 'branch')))
    file_content = file_content.replace("${{commit_id}}", json.dumps(get_value_safty(params, 'commit_id')))
    file_content = file_content.replace("${{type}}", json.dumps(get_value_safty(params, 'type')))
    file_content = file_content.replace("${{project_version}}", json.dumps(get_value_safty(params, 'project_version')))
    file_content = file_content.replace("${{host_user}}", json.dumps(get_value_safty(params, 'host_user')))
    file_content = file_content.replace("${{git_credentials_id}}", json.dumps(get_value_safty(params, 'git_credentials_id')))
    file_content = file_content.replace("${{deploy_sleep_seconds}}", str(get_value_safty(params, 'deploy_sleep_seconds')))
    file_content = file_content.replace("${{sleep_seconds_after_kill}}", str(get_value_safty(params, 'sleep_seconds_after_kill')))
    file_content = file_content.replace("${{is_backup}}", boolean_convertor(get_value_safty(params, 'is_backup')))
    file_content = file_content.replace("${{to_tag}}", boolean_convertor(get_value_safty(params, 'to_tag')))
    file_content = file_content.replace("${{code_static_check}}", boolean_convertor(get_value_safty(params, 'code_static_check')))
    file_content = file_content.replace("${{unit_test}}", boolean_convertor(get_value_safty(params, 'unit_test')))
    file_content = file_content.replace("${{maven_package}}", boolean_convertor(get_value_safty(params, 'maven_package')))
    file_content = file_content.replace("${{maven_install}}", boolean_convertor(get_value_safty(params, 'maven_install')))
    file_content = file_content.replace("${{to_deploy}}", boolean_convertor(get_value_safty(params, 'to_deploy')))
    file_content = file_content.replace("${{deploy_stopping}}", boolean_convertor(get_value_safty(params, 'deploy_stopping')))
    deploy_stopping_timeout_seconds = get_value_safty(params, 'deploy_stopping_timeout_seconds')
    deploy_stopping_timeout_seconds = 1 if deploy_stopping_timeout_seconds == '' else deploy_stopping_timeout_seconds
    file_content = file_content.replace("${{deploy_stopping_timeout_seconds}}", str(deploy_stopping_timeout_seconds))
    file_content = file_content.replace("${{increment}}", str(get_value_safty(params, 'increment')))

    # web special.
    file_content = file_content.replace("${{npm_home}}", json.dumps(get_value_safty(params, 'npm_home')))
    file_content = file_content.replace("${{yarn_home}}", json.dumps(get_value_safty(params, 'yarn_home')))
    file_content = file_content.replace("${{npm_registry_url}}", json.dumps(get_value_safty(params, 'npm_registry_url')))
    file_content = file_content.replace("${{package_type}}", json.dumps(get_value_safty(params, 'package_type')))
    file_content = file_content.replace("${{to_compile}}", json.dumps(get_value_safty(params, 'to_compile')))
    file_content = file_content.replace("${{build_param}}", json.dumps(get_value_safty(params, 'build_param')))

    # docker special.
    docker_info = params.get('docker_info')
    default_app_port = 8080
    if docker_info is not None and boolean_convertor(get_value_safty(docker_info, 'with_docker')) == 'true':
        file_content = file_content.replace("${{with_docker}}", boolean_convertor(get_value_safty(docker_info, 'with_docker')))
        file_content = file_content.replace("${{dockerfile_project_dockerfile_name}}", json.dumps(get_value_safty(docker_info, 'dockerfile_project_dockerfile_name')))
        file_content = file_content.replace("${{dockerfile_project_git_url}}", json.dumps(get_value_safty(docker_info, 'dockerfile_project_git_url')))
        file_content = file_content.replace("${{elk_topic}}", json.dumps(get_value_safty(docker_info, 'elk_topic')))
        file_content = file_content.replace("${{elk_kafka_cluster_list}}", json.dumps(get_value_safty(docker_info, 'elk_kafka_cluster_list')))
        file_content = file_content.replace("${{docker_repo}}", json.dumps(get_value_safty(docker_info, 'docker_repo')))
        app_port = get_value_safty(params, 'app_port')
        app_port = default_app_port if app_port == '' else app_port
        file_content = file_content.replace("${{app_port}}", str(app_port))
        file_content = file_content.replace("${{docker_other_params}}", json.dumps(get_value_safty(docker_info, 'docker_other_params')))
    else:
        file_content = file_content.replace("${{with_docker}}", 'false')
        file_content = file_content.replace("${{dockerfile_project_dockerfile_name}}", "\"\"")
        file_content = file_content.replace("${{dockerfile_project_git_url}}", "\"\"")
        file_content = file_content.replace("${{elk_topic}}", "\"\"")
        file_content = file_content.replace("${{elk_kafka_cluster_list}}", "\"\"")
        file_content = file_content.replace("${{docker_repo}}", "\"\"")
        file_content = file_content.replace("${{app_port}}", str(default_app_port))
        file_content = file_content.replace("${{docker_other_params}}", "\"\"")
    return file_content


# def generate_pipeline_lite(
#         git_url,
#         target_hosts,
#         app_name,
#         env,
#         type,
#         project_version,
#         git_credentials_id
# ):
#     return generate_pipeline(
#         git_url=git_url,
#         maven_home=maven_home,
#         maven_settings_file_path=maven_settings_file_path,
#         java_home=java_home,
#         target_hosts=target_hosts,
#         app_name=app_name,
#         env=env,
#         app_home=app_home,
#         branch=branch,
#         type=type,
#         project_version=project_version,
#         host_user=host_user,
#         git_credentials_id=git_credentials_id,
#         deploy_sleep_seconds=deploy_sleep_seconds,
#         is_backup=is_backup,
#         to_tag=to_tag,
#         code_static_check=code_static_check,
#         unit_test=unit_test,
#         maven_package=maven_package
#     )

def boolean_convertor(boolean):
    if boolean:
        return 'true'
    else:
        return 'false'

def write_content_to_file(scm_url, namespace, type, project_name, app_name, env, content):
    create_dir_path = "./{3}/{0}/{1}-{2}/".format(namespace, project_name, env, get_git_saved_location(scm_url))
    create_file_path = create_dir_path + "{1}-{0}.groovy".format(app_name, type)
    if os.path.exists(create_dir_path) is False:
        os.makedirs(create_dir_path)
    write_file(create_file_path, content)
    return create_file_path