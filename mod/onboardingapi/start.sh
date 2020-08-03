#!/bin/bash

if [ -z $PG_CONN ]; then
    echo "PG_CONN variable has not been set"
    echo "PG_CONN contains the full postgresql URI"
    exit 1
fi

mkdir -p ~/.config/dcae-cli
if [ ! -f ~/.config/dcae-cli/config.json ]; then
    echo "Creating dcae-cli config"
    echo "{\"server_url\": \"$SERVER_URL\", \"db_url\": \"$PG_CONN\", \"path_component_spec\": \"$PATH_COMPONENT_SPEC\", \"path_data_format\": \"$PATH_DATA_FORMAT\"}" > ~/.config/dcae-cli/config.json
fi

dcae_cli http --live
