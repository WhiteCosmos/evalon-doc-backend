#!/usr/bin/env bash
export GRAILS_OPTS="-Xmx14G -Xms8G "
git pull; nohup grails run-app &