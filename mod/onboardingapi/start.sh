#!/bin/bash

if [ -z $PG_CONN ]; then
    echo "PG_CONN variable has not been set"
    echo "PG_CONN contains the full postgresql URI"
    exit 1
fi

if [ ! -f ~/.config/dcae-cli/config.json ]; then
    echo "Creating dcae-cli config"
    # TODO: Make this into a variable that gets fed in via docker run
    echo "{\"server_url\": \"https://git.onap.org/dcaegen2/platform/plain/mod\", \"user\": \"api\", \"db_url\": \"$PG_CONN\", \"cli_version\": \"2.12.0\", \"path_component_spec\": \"/component-json-schemas/component-specification/dcae-cli-v2/component-spec-schema.json\", \"path_data_format\": \"/component-json-schemas/data-format/dcae-cli-v1/data-format-schema.json\"}" > ~/.config/dcae-cli/config.json
fi

dcae_cli http --live
