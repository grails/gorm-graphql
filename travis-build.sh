#!/bin/bash
EXIT_STATUS=0

./gradlew --stop
./gradlew --no-daemon compileGroovy
./gradlew --no-daemon compileTestGroovy

if [[ $TRAVIS_TAG =~ ^v[[:digit:]] ]]; then
	echo "Skipping tests to Publish release"
	./travis-publish.sh || EXIT_STATUS=$?
else
	./gradlew check --refresh-dependencies   || EXIT_STATUS=$?
	
	if [[ $EXIT_STATUS -eq 0 ]]; then
	    ./travis-publish.sh || EXIT_STATUS=$?
	fi

fi

exit $EXIT_STATUS



