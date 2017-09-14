#!/bin/bash
echo "Publishing..."

EXIT_STATUS=0

if [[ $TRAVIS_REPO_SLUG == "grails/gorm-graphql" && $TRAVIS_PULL_REQUEST == 'false' && $EXIT_STATUS -eq 0 ]]; then

  echo "Publishing archives"
  export GRADLE_OPTS="-Xmx1500m -Dfile.encoding=UTF-8"

  if [[ $TRAVIS_TAG =~ ^v[[:digit:]] ]]; then

    if [[ $EXIT_STATUS -eq 0 ]]; then
        ./gradlew publish --no-daemon || EXIT_STATUS=$?
    fi

    if [[ $EXIT_STATUS -eq 0 ]]; then
        ./gradlew bintrayUpload --no-daemon || EXIT_STATUS=$?
    fi
  else
    # for snapshots only to repo.grails.org
    ./gradlew  publish || EXIT_STATUS=$?
  fi

  if [[ $EXIT_STATUS -eq 0 ]]; then
    echo "Publishing Successful."
  fi


  if [[ $EXIT_STATUS -eq 0 ]]; then
    echo "Publishing Successful."

    echo "Publishing Documentation..."
    ./gradlew docs:docs

    git config --global user.name "$GIT_NAME"
    git config --global user.email "$GIT_EMAIL"
    git config --global credential.helper "store --file=~/.git-credentials"
    echo "https://$GH_TOKEN:@github.com" > ~/.git-credentials


    git clone https://${GH_TOKEN}@github.com/grails/gorm-graphql.git -b gh-pages gh-pages --single-branch > /dev/null
    cd gh-pages

    if [[ -n $TRAVIS_TAG ]]; then
        version="$TRAVIS_TAG"
        version=${version:1}

        majorVersion=${version:0:4}
        majorVersion="${majorVersion}x"

        mkdir -p "$version"
        cp -r ../docs/build/docs/. "./$version/"
        git add "$version/*"

        mkdir -p "$majorVersion"
        cp -r ../docs/build/docs/. "./$majorVersion/"
        git add "$majorVersion/*"

        mkdir -p latest
        cp -r ../docs/build/docs/. ./latest/
        git add latest/*

    else
        # If this is the master branch then update the snapshot
        mkdir -p snapshot
        cp -r ../docs/build/docs/. ./snapshot/

        git add snapshot/*
    fi


    git commit -a -m "Updating GraphQL Docs for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
    git push origin HEAD
    cd ../../..
    rm -rf gh-pages
  fi  
fi

exit $EXIT_STATUS
