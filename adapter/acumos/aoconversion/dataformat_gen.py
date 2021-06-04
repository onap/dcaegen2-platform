# ============LICENSE_START====================================================
# org.onap.dcae
# =============================================================================
# Copyright (c) 2019-2020 AT&T Intellectual Property. All rights reserved.
# Copyright (c) 2021 highstreet technologies GmbH. All rights reserved.
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

from subprocess import PIPE, Popen
import json
from jsonschema import validate
from aoconversion import utils, exceptions


def _protobuf_to_js(proto_path):
    """
    Converts a protobuf to jsonschema and returns the generated schema as a JSON object.
    """
    cmd = ["protobuf-jsonschema", proto_path]
    p = Popen(cmd, stderr=PIPE, stdout=PIPE)
    out = p.stdout.read()
    asjson = json.loads(out)

    # change the defintion names to remove the random package name that acumos generates
    defs = asjson["definitions"]
    defns = list(defs.keys())
    for defn in defns:
        # https://stackoverflow.com/questions/16475384/rename-a-dictionary-key
        defs[defn.split(".")[1]] = defs.pop(defn)

    # make sure what we got out is a valid jsonschema
    draft4 = utils.schema_schema.get()
    validate(instance=asjson, schema=draft4)

    return asjson


def _get_needed_formats(meta):
    """
    Read the metadata and figure out what the principle data formats are.
    We cannot determine this from the proto because the proto may list "submessages" in a flat namespace; some of them may not coorespond to a data format but rather a referenced defintion in another.
    We don't want to generate a data format for submessages though; instead they should be included in definitions as part of the relevent data format
    """
    # we use a dict because multiple methods may reuse names
    needed_formats = {}
    for method in meta["methods"]:
        needed_formats[utils.validate_format(meta, method, "input")] = 1
        needed_formats[utils.validate_format(meta, method, "output")] = 1
    return list(needed_formats.keys())


def _generate_dcae_data_formats(proto_path, meta, dcae_df_schema, draft_4_schema):
    """
    Generates a collection of data formats from the model .proto
    This helper function is broken out for the ease of unit testing; this can be unit tested easily because all deps are parameters,
    but generate_dcae_data_formats requires some mocking etc.
    """
    js = _protobuf_to_js(proto_path)
    needed_formats = _get_needed_formats(meta)

    data_formats = []

    used_defns = []

    # iterate over and convert
    for nf in needed_formats:
        defn = js["definitions"][nf]

        definitions = {}

        # check for the case where we have an array of other defns
        for prop in defn["properties"]:
            if defn["properties"][prop]["type"] == "array" and "$ref" in defn["properties"][prop]["items"]:
                unclean_ref_name = defn["properties"][prop]["items"]["$ref"]
                clean_ref_name = unclean_ref_name.split(".")[1]
                if clean_ref_name in js["definitions"]:
                    defn["properties"][prop]["items"]["$ref"] = "#/definitions/{0}".format(clean_ref_name)
                    definitions[clean_ref_name] = js["definitions"][clean_ref_name]
                    used_defns.append(clean_ref_name)
                else:  # this is bad/unsupported, investigate
                    raise exceptions.UnsupportedFormatScenario()

        # the defns created by this tool do not include a schema field.
        # I created an issue: https://github.com/devongovett/protobuf-jsonschema/issues/12
        defn["$schema"] = "http://json-schema.org/draft-04/schema#"

        # Include the definitions, which may be empty {}
        defn["definitions"] = definitions

        # Validate that our resulting jsonschema is valid jsonschema
        validate(instance=defn, schema=draft_4_schema)

        # we currently hardcode dataformatversion, since it is the latest and has been for years  https://gerrit.onap.org/r/gitweb?p=dcaegen2/platform/cli.git;a=blob_plain;f=component-json-schemas/data-format/dcae-cli-v1/data-format-schema.json;hb=HEAD
        dcae_df = {"self": {"name": nf, "version": "1.0.0"}, "dataformatversion": "1.0.1", "jsonschema": defn}

        # make sure the schema validates against the DCAE data format schema
        validate(instance=dcae_df, schema=dcae_df_schema)

        # if we've passed the validation and exc raising so far, we are good, append this to output list of dcae data formats
        data_formats.append(dcae_df)

    # make sure every definitin we got out was used. Otherwise, this requires investigation!!
    if sorted(needed_formats + used_defns) != sorted(list(js["definitions"].keys())):
        raise exceptions.UnsupportedFormatScenario()

    return data_formats


# Public


def generate_dcae_data_formats(model_repo_path, model_name):
    """
    Generates a collection of data formats from the model .proto
    Writes them to disk
    Returns them as the return of this call so this can be fed directly into spec gen
    """
    data_formats = _generate_dcae_data_formats(
        "{0}/{1}/model.proto".format(model_repo_path, model_name),
        utils.get_metadata(model_repo_path, model_name),
        utils.dataformat_schema.get(),
        utils.schema_schema.get()
    )

    # now we iterate over these and write a file to disk for each, since the dcae cli seems to want that
    for df in data_formats:
        # name_version seems like a reasonable filename
        fname = "{0}_{1}_dcae_data_format.json".format(df["self"]["name"], df["self"]["version"])
        with open("{0}/{1}".format(model_repo_path, fname), "w") as f:
            f.write(json.dumps(df))

    return data_formats
