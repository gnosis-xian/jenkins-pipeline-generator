import os

from constants import *


def pull_from_git():
    os.system("git clone " + git_saved_url)

def add(file_path):
    os.system("cd {} && git add {}".format(git_saved_location, file_path))

def commit(project_name, app_name, env):
    os.system("cd {} && git commit -m \"commit {}: {}-{} script.\"".format(git_saved_location, project_name, app_name, env))

def push():
    os.system("cd {} && git push".format(git_saved_location))

def git_actions(file_path, project_name, app_name, env):
    # pull_from_git()
    add(file_path)
    commit(project_name, app_name, env)
    push()

if __name__ == '__main__':
    git_saved_location = "git@gitee.com:accessgnosis/jenkins-pipeline-scripts.git"
    pull_from_git()
    add("yto/share-dev/share-app.groovy")
    commit("share", "share-app", "dev")
    push()
    pass