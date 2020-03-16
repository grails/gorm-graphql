#!/bin/bash
EXIT_STATUS=0

./gradlew --stop
./gradlew --no-daemon testClasses

./gradlew --no-daemon check --stacktrace || EXIT_STATUS=$?


if [[ $EXIT_STATUS -eq 0 ]]; then
	./travis-publish.sh || EXIT_STATUS=$?
else
  if [[ -d ./core/build/reports ]]; then
    git config --global user.name "$GIT_NAME"
    git config --global user.email "$GIT_EMAIL"
    git config --global credential.helper "store --file=~/.git-credentials"
    echo "https://$GH_TOKEN:@gidsthub.com" > ~/.git-credentials

    echo "Publishing test reports"
    git clone https://${GH_TOKEN}@github.com/grails/gorm-graphql.git -b test-reports test-reports --single-branch > /dev/null
    cd test-reports
    rm -rf latest
    mkdir -p latest
    cp -r ../core/build/reports/. ./latest/
    git add latest/*
    git commit -a -m "Updating GraphQL Test Reports for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
    git push origin HEAD
    cd ../../..
    rm -rf test-reports
  fi
fi

exit $EXIT_STATUS
