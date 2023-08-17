#!/usr/bin/env bash
set -xuev

docker run \
  --rm \
  --platform linux/amd64 \
  -p 9876:9876 \
  registry.fly.io/scala-slack-bot:latest