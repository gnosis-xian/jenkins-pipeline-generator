import os
import platform

from flask import Flask, render_template
from flask import request
from pipeline_scripts_generator import *
from git_utils import *
import json

app = Flask(__name__)


@app.route('/gen-jenkins-pipeline/java-api', methods=['POST'])
def gen_jenkins_pipeline():

    before()

    data = request.get_data()
    req_json = json.loads(data)

    namespace = req_json['namespace']
    project_name = req_json['project_name']
    jenkins_properties = req_json['jenkins_properties']

    git_url = jenkins_properties['git_url']
    maven_home = jenkins_properties['maven_home']
    maven_settings_file_path = jenkins_properties['maven_settings_file_path']
    java_home = jenkins_properties['java_home']
    target_hosts = jenkins_properties['target_hosts']
    app_name = jenkins_properties['app_name']
    env = jenkins_properties['env']
    app_home = jenkins_properties['app_home']
    branch = jenkins_properties['branch']
    type = jenkins_properties['type']
    project_version = jenkins_properties['project_version']
    host_user = jenkins_properties['host_user']
    git_credentials_id = jenkins_properties['git_credentials_id']
    deploy_sleep_seconds = jenkins_properties['deploy_sleep_seconds']
    is_backup = jenkins_properties['is_backup']
    to_tag = jenkins_properties['to_tag']
    code_static_check = jenkins_properties['code_static_check']
    unit_test = jenkins_properties['unit_test']
    maven_package = jenkins_properties['maven_package']

    jenkins_content = generate_pipeline(
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

    jenkins_pipeline_path = write_content_to_file(namespace, project_name, app_name, env, jenkins_content)

    file_path = "{}/{}-{}/{}.groovy".format(namespace, project_name, env, app_name)

    git_actions(file_path, project_name, app_name, env)

    result = {
        "Repository URL": git_saved_url,
        "Branch Specifier": "*/master",
        "Script Path": file_path
    }
    delete_git_dir()

    return json.dumps(result)

def before():
    delete_git_dir()
    pull_from_git()

def delete_git_dir():
    if os.path.exists(git_saved_location):
        if platform.system().lower() == "windows":
            os.system("RD /S /Q " + git_saved_location)
        elif platform.system().lower() == "linux":
            os.system("rm -rf " + git_saved_location)

if __name__ == '__main__':
    app.run(debug=True)
