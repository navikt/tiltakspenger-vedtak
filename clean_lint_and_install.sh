#!/bin/bash
./gradlew clean spotlessApply build installDist "$@"
