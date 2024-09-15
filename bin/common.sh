#!/bin/bash

PACKR_JAR_PATH=/Applications/packr/packr-all-4.0.0.jar
# get the right JRE from here: https://www.azul.com/downloads/?version=java-11-lts&os=macos&package=jre-fx#zulu
JDK_PATH=/Applications/packr/zulu11.74.15-ca-fx-jre11.0.24-macosx_x64.tar.gz
TARGET_DIR=build/artifacts
TARGET_LOCATION_APP=${TARGET_DIR}/AllFit.app
SHADOW_JAR_LOCATION=build/libs/allfit-all.jar

buildArtifactWithVersion () {
  VERSION=$1
  echo "Building Gradle artifact with version: [$VERSION] ..."
  ./gradlew clean check shadowJar -Pallfit.version="$VERSION" -Dorg.gradle.java.home=/Users/toh/.jenv/versions/11/ || exit 1
}

sanityCheck() {
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
}

packageDeployables () {
  sanityCheck
  rm -rf $TARGET_DIR
  mkdir $TARGET_DIR

  echo "Packaging macOS native app ..."
  java -jar $PACKR_JAR_PATH \
       --platform mac \
       --jdk $JDK_PATH \
       --executable AllFit \
       --classpath $SHADOW_JAR_LOCATION \
       --mainclass allfit.AllFit \
       --bundle org.cpickl.allfit \
       --icon src/macapp/logo.icns \
       --vmargs Xmx4G \
       --vmargs Xms1G \
       --vmargs Dallfit.env=PROD \
       --output $TARGET_LOCATION_APP || exit 1
  plutil -replace CFBundleShortVersionString -string $VERSION $TARGET_LOCATION_APP/Contents/Info.plist

  mv $SHADOW_JAR_LOCATION $TARGET_DIR/AllFit.jar
}
