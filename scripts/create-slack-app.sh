#!/usr/bin/env bash

set -o errexit -o verbose -o xtrace

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source "$SCRIPT_DIR/../resources/app-conf.sh"
export APP_NAME=${APP_NAME:-Scala Slack Bot}

manifest="$( cat ${SCRIPT_DIR}/../resources/slack-manifest.json  | envsubst )"

echo $manifest

function createApp() {
  curl -s -XPOST https://slack.com/api/apps.manifest.create \
    -H "Authorization: Bearer ${APP_CONF_TOKEN}" \
    -d "manifest=$manifest"
}

response=$(createApp)
app_id=$(echo $response | jq '.app_id')

echo "Go to https://api.slack.com/apps/${app_id}/oauth and install the app"