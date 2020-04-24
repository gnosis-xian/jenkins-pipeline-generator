import json


class Host:
    host = ""
    port = ""


if __name__ == '__main__':
    list = []
    list.append(["192.168.207.50", "22"])
    list.append(["192.168.207.49", "22"])

    map = {
        "list": [
            {
                "host": "123",
                "port": "22"
            }
        ]
    }

    print(json.dumps(map))

    map_str = "{\"list\": [{\"host\": \"123\", \"port\": \"22\"}]}"
    map = json.loads(map_str)
    print(map["list"][0]["host"])

    file = open("../template/jenkins-pipeline-java-api.groovy")
    file_content = file.read()
    file_content = file_content.replace("${{git_url}}", "http://github.com/")
    print(file_content)
