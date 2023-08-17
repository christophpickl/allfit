#!/bin/bash

source "bin/common.sh"

VERSION_FILE=./version.txt
APP_VERSION=$(($(cat $VERSION_FILE) + 1))

git_status_verify () {
  git status
  echo
  echo "Verify no GIT changes are uncommitted and hit ENTER to continue."
  read
}

git_tag_and_push () {
  echo "Incrementing and tagging version number via GIT ..."
  echo $APP_VERSION > $VERSION_FILE
  git add .
  git commit -m "Increment version number to $APP_VERSION"
  git tag -a $APP_VERSION -m "Tag new release version $APP_VERSION"
  git push
  git push origin $APP_VERSION
}

git_status_verify
buildArtifactWithVersion $APP_VERSION
packageDeployables
git_tag_and_push

echo ""
echo "Success ✅"
echo ""
echo "Now create a new release in GitHub to finish it up:"
echo "https://github.com/christophpickl/allfit/releases"

exit 0
