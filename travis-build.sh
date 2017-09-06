#!/bin/bash
EXIT_STATUS=0

./gradlew --stop
./gradlew --no-daemon compileGroovy
./gradlew --no-daemon compileTestGroovy

./gradlew --no-daemon check || EXIT_STATUS=$?

if [[ $EXIT_STATUS -eq 0 ]]; then
	./travis-publish.sh || EXIT_STATUS=$?
fi

exit $EXIT_STATUS
