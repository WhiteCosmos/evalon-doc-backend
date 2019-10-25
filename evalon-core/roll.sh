#!/usr/bin/env bash
export GRAILS_OPTS="-Xmx14G -Xms8G "
git pull; grails stop; nohup grails run-app &