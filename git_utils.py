import os

github_http_prefix = "https://github.com/"
gitee_http_prefix = "https://gitee.com/"
github_ssh_prefix = "git@github.com:"
gitee_ssh_prefix = "git@gitee.com:"

def pull_from_git(scm_url):
    os.system("git clone " + scm_url)

def add(scm_url, file_path):
    os.system("cd {} && git add {}".format(get_git_saved_location(scm_url), file_path))

def commit(scm_url, project_name, app_name, env):
    os.system("cd {} && git commit -m \"commit {}: {}-{} script.\"".format(get_git_saved_location(scm_url), project_name, app_name, env))

def push(scm_url):
    os.system("cd {} && git push".format(get_git_saved_location(scm_url)))

def git_actions(scm_url, file_path, project_name, app_name, env):
    # pull_from_git(scm_url)
    add(scm_url, file_path)
    commit(scm_url, project_name, app_name, env)
    # push(scm_url)

def convert_http2SSH(http_url):
    if http_url.startswith(github_http_prefix):
        return http_url.replace(github_http_prefix, github_ssh_prefix)
    elif http_url.startswith(gitee_http_prefix):
        return http_url.replace(gitee_http_prefix, gitee_ssh_prefix)
    return http_url

def convert_SSH2Http(ssh_url):
    if ssh_url.startswith(github_ssh_prefix):
        return ssh_url.replace(github_ssh_prefix, github_http_prefix)
    elif ssh_url.startswith(gitee_ssh_prefix):
        return ssh_url.replace(gitee_ssh_prefix, gitee_http_prefix)
    return ssh_url

def get_git_saved_location(url):
    git_saved_location = url[url.rindex("/")+1:].replace(".git", "");
    return git_saved_location


if __name__ == '__main__':
    # git_saved_location = "git@gitee.com:accessgnosis/jenkins-pipeline-scripts.git"
    # pull_from_git()
    # add("yto/share-dev/share-app.groovy")
    # commit("share", "share-app", "dev")
    # push()
    # url = "https://gitee.com/accessgnosis/jenkins-files.git"
    url = "git@gitee.com:accessgnosis/jenkins-files.git"
    print(get_git_saved_location(url))
    pass