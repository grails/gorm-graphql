#!/bin/bash
EXIT_STATUS=0

./gradlew --stop
./gradlew --no-daemon testClasses

./gradlew --no-daemon check --stacktrace || EXIT_STATUS=$?


if [[ $EXIT_STATUS -eq 0 ]]; then
	./travis-publish.sh || EXIT_STATUS=$?
fi

exit $EXIT_STATUS
