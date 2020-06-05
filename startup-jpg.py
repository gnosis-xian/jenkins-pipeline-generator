import os
import platform

from flask import Flask, render_template
from flask import request
from pipeline_scripts_generator import *
from git_utils import *
from constants import *
from services import *
import json

app = Flask(__name__)

@app.route('/gen-jenkins-pipeline/web', methods=['POST'])
def gen_jenkins_pipeline_web():
    resultBody = None
    try:
        resultBody = go(request, "web")
    except Exception as e:
        print(e)
        remove_lock()
    return resultBody

@app.route('/gen-jenkins-pipeline/java-api', methods=['POST'])
def gen_jenkins_pipeline_api():
    resultBody = None
    try:
        resultBody = go(request, "api")
    except Exception as e:
        print(e)
        remove_lock()
    return resultBody

def go(request, type):
    if locked():
        return "Still has another work process... Please try again later."
    create_lock()

    data = request.get_data()
    req_json = json.loads(data)
    scm_urls = req_json['scm_urls']

    namespace = req_json['namespace']
    project_name = req_json['project_name']
    jenkins_properties = req_json['jenkins_properties']

    scm_http_urls = []
    file_path_list = []

    for scm_url in scm_urls:
        scm_http_urls.append(convert_SSH2Http(scm_url))
        before(scm_url)

        for jenkins_propertie in jenkins_properties:
            file_path = ''
            if type == "web":
                file_path = web_service(namespace, project_name, scm_url, jenkins_propertie)
            elif type == "api":
                file_path = java_api_service(namespace, project_name, scm_url, jenkins_propertie)
            file_path_list.append(file_path)

        push(scm_url)
        delete_git_dir(scm_url)

    result = {
        "Repository URL": scm_http_urls,
        "Branch Specifier": "*/master",
        "Script Path": list(set(file_path_list))
    }

    remove_lock()
    return json.dumps(result)

def before(scm_url):
    delete_git_dir(scm_url)
    pull_from_git(scm_url)

def delete_git_dir(scm_url):
    if os.path.exists(get_git_saved_location(scm_url)):
        if platform.system().lower() == "windows":
            os.system("RD /S /Q " + get_git_saved_location(scm_url))
        elif platform.system().lower() == "linux":
            os.system("rm -rf " + get_git_saved_location(scm_url))

def locked():
    return os.path.exists(lock_file)

def create_lock():
    if os.path.exists(lock_file) is False:
        with open(lock_file, "w") as file:
            file.write("")

def remove_lock():
    if os.path.exists(lock_file):
        os.remove(lock_file)

if __name__ == '__main__':
    remove_lock()
    app.run(host="0.0.0.0", port=5000, debug=False)
