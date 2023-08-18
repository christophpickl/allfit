#!/bin/bash

PACKR_JAR_PATH=/Applications/packr/packr-all-4.0.0.jar
JDK_PATH=/Applications/packr/OpenJDK11U-jre_x64_mac_hotspot_11.0.19_7.tar.gz
TARGET_LOCATION=build/AllFit.app

buildArtifactWithVersion () {
  VERSION=$1
  echo "Building Gradle artifact with version: [$VERSION] ..."
  ./gradlew clean check shadowJar -Pallfit.version="$VERSION" || exit 1
}

packageDeployables () {
  echo "Packaging macOS native app ..."

  if [ -e $TARGET_LOCATION ]
  then
    echo "Removing old APP file at: $TARGET_LOCATION"
    rm -rf $TARGET_LOCATION
  fi

  if [ -e $PACKR_JAR_PATH ]
  then
    echo "Using packr located at: $PACKR_JAR_PATH"
  else
    echo "ERROR: Unable to locate packr! Please download the latest version from https://github.com/libgdx/packr and place it to $PACKR_JAR_PATH"
    exit 1
  fi

  if [ -e $JDK_PATH ]
  then
    echo "Using JDK located at: $JDK_PATH"
  else
    echo "ERROR: Unable to locate JDK! Please download the latest version from https://adoptopenjdk.net/releases.html and place it to $JDK_PATH"
    exit 1
  fi

  java -jar $PACKR_JAR_PATH \
       --platform mac \
       --jdk $JDK_PATH \
       --executable AllFit \
       --classpath build/libs/allfit-all.jar \
       --mainclass allfit.AllFit \
       --icon src/macapp/logo.icns \
       --vmargs Xmx4G \
       --vmargs Xms1G \
       --vmargs Dallfit.env=PROD \
       --output $TARGET_LOCATION || exit 1
}
