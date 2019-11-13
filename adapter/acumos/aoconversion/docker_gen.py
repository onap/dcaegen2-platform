# ============LICENSE_START====================================================
# org.onap.dcae
# =============================================================================
# Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
# =============================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END======================================================

import os
from docker import APIClient
from aoconversion import exceptions, utils


def _generate_dockerfile(meta, model_name):
    """
    bind the templated docker string
    """
    docker_template = """
    FROM python:{VERSION}

    ENV MODELNAME {MODELNAME}
    RUN mkdir /app
    WORKDIR /app

    ADD ./{MODELNAME} /app/{MODELNAME}
    ADD ./requirements.txt /app

    RUN pip install -r /app/requirements.txt && \
        pip install acumos_dcae_model_runner

    ENV DCAEPORT=10000
    EXPOSE $DCAEPORT

    ENTRYPOINT ["acumos_dcae_model_runner"]
    CMD ["/app/{MODELNAME}"]
    """
    python_version = meta["runtime"]["version"]
    return docker_template.format(VERSION=python_version, MODELNAME=model_name)


# Public


def build_and_push_docker(config, model_name, model_version="latest"):
    """
    build and push the dcae docker container
    Returns the docker uri so this can be pipelined into specgen
    """
    model_repo_path = config.tmpdir
    dockerfile_path = "{0}/Dockerfile".format(model_repo_path)
    reqs_path = "{0}/requirements.txt".format(model_repo_path)

    # get the metadata
    meta = utils.get_metadata(model_repo_path, model_name)

    # write the reqs file, will be removed later
    reqs = meta["runtime"]["dependencies"]["pip"]["requirements"]
    with open(reqs_path, "w") as f:
        for r in reqs:
            f.write("{0}=={1}\n".format(r["name"], r["version"]))

    # generate the dockerfile
    dockerfile = _generate_dockerfile(meta, model_name)

    # write the dockerfile, will be removed later
    with open("{0}/Dockerfile".format(model_repo_path), "w") as f:
        f.write(dockerfile)

    docker_uri = "{0}/{1}:{2}".format(config.dockerregistry, model_name, model_version)

    # do the docker build
    cli = APIClient(base_url=config.dockerhost, user_agent="Docker-Client-xx.yy")
    response = [line.decode() for line in cli.build(path=model_repo_path, rm=True, tag=docker_uri)]

    # clean up the files
    os.remove(dockerfile_path)
    os.remove(reqs_path)

    # parse the Docker response to see whether we succeeded
    for r in response:
        # In some scenarios, for example a non-existing Dockerfile, docker build raises a native exception, see:
        # https://docker-py.readthedocs.io/en/stable/api.html#module-docker.api.build
        # However, if something fails such as a non-existing directory referenced in the dockerfile, NO exception is raised.
        # In this case, one of the console output lines is "errorDetail"....
        if "errorDetail" in r:
            raise exceptions.DockerBuildFail(r)

    # if the above succeeded, we can push
    # push. same problem as above; e.g., "no basic auth credentials" does not throw an exception!
    response = [
        line.decode()
        for line in cli.push(
            repository=docker_uri, auth_config={"username": config.dockeruser, "password": config.dockerpass()}, stream=True
        )
    ]
    for r in response:
        if "errorDetail" in r:
            raise exceptions.DockerPushFail(r)

    return docker_uri
