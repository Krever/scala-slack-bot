#!/usr/bin/env bash
set -xuev

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

manifest="$( cat ${SCRIPT_DIR}/../resources/slack-manifest.json  | envsubst )"

echo $manifest

function createApp() {
  curl -s -XPOST https://slack.com/api/apps.manifest.update \
    -H "Authorization: Bearer ${APP_CONF_TOKEN}" \
    -d "manifest=$manifest" \
    -d "app_id=${APP_ID}"
}

response=$(createApp)

echo $response