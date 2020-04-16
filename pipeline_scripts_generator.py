import json
import os
from constants import *

def read_file_content(file_path):
    file = open(file_path, "r")
    return file.read()

def write_file(file_path, write_content):
    with open(file_path, "w") as file:
        file.write(write_content)


def generate_pipeline(**params):
    file_content = read_file_content("template/jenkins-pipeline.groovy")
    file_content = file_content.replace("${{git_url}}", json.dumps(params['git_url']))
    file_content = file_content.replace("${{maven_home}}", json.dumps(params['maven_home']))
    file_content = file_content.replace("${{maven_settings_file_path}}", json.dumps(params['maven_settings_file_path']))
    file_content = file_content.replace("${{java_home}}", json.dumps(params['java_home']))
    file_content = file_content.replace("${{target_hosts}}", json.dumps(params['target_hosts']))
    file_content = file_content.replace("${{app_name}}", json.dumps(params['app_name']))
    file_content = file_content.replace("${{env}}", json.dumps(params['env']))
    file_content = file_content.replace("${{app_home}}", json.dumps(params['app_home'] + "/$app_name-$env"))
    file_content = file_content.replace("${{branch}}", json.dumps(params['branch']))
    file_content = file_content.replace("${{type}}", json.dumps(params['type']))
    file_content = file_content.replace("${{project_version}}", json.dumps(params['project_version']))
    file_content = file_content.replace("${{host_user}}", json.dumps(params['host_user']))
    file_content = file_content.replace("${{git_credentials_id}}", json.dumps(params['git_credentials_id']))
    file_content = file_content.replace("${{deploy_sleep_seconds}}", str(params['deploy_sleep_seconds']))
    file_content = file_content.replace("${{is_backup}}", boolean_convertor(params['is_backup']))
    file_content = file_content.replace("${{to_tag}}", boolean_convertor(params['to_tag']))
    file_content = file_content.replace("${{code_static_check}}", boolean_convertor(params['code_static_check']))
    file_content = file_content.replace("${{unit_test}}", boolean_convertor(params['unit_test']))
    file_content = file_content.replace("${{maven_package}}", boolean_convertor(params['maven_package']))
    return file_content


def generate_pipeline_lite(
        git_url,
        target_hosts,
        app_name,
        env,
        type,
        project_version,
        git_credentials_id
):
    return generate_pipeline(
        git_url=git_url,
        maven_home=maven_home,
        maven_settings_file_path=maven_settings_file_path,
        java_home=java_home,
        target_hosts=target_hosts,
        app_name=app_name,
        env=env,
        app_home=app_home,
        branch=branch,
        type=type,
        project_version=project_version,
        host_user=host_user,
        git_credentials_id=git_credentials_id,
        deploy_sleep_seconds=deploy_sleep_seconds,
        is_backup=is_backup,
        to_tag=to_tag,
        code_static_check=code_static_check,
        unit_test=unit_test,
        maven_package=maven_package
    )


def boolean_convertor(boolean):
    if boolean:
        return 'true'
    else:
        return 'false'

def write_content_to_file(namespace, project_name, app_name, env, content):
    create_dir_path = "./{3}/{0}/{1}-{2}/".format(namespace, project_name, env, git_saved_location)
    create_file_path = create_dir_path + "{0}.groovy".format(app_name)
    if os.path.exists(create_dir_path) is False:
        os.makedirs(create_dir_path)
    write_file(create_file_path, content)
    return create_file_path
