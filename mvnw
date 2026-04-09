#!/bin/sh

set -eu

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
PROPERTIES_FILE="$WRAPPER_DIR/maven-wrapper.properties"

if [ ! -f "$PROPERTIES_FILE" ]; then
  echo "Missing $PROPERTIES_FILE" >&2
  exit 1
fi

DISTRIBUTION_URL=$(grep '^distributionUrl=' "$PROPERTIES_FILE" | cut -d'=' -f2-)
MAVEN_VERSION=$(printf '%s' "$DISTRIBUTION_URL" | sed -n 's|.*apache-maven/\([0-9.]*\)/.*|\1|p')

if [ -z "$MAVEN_VERSION" ]; then
  MAVEN_VERSION=$(printf '%s' "$DISTRIBUTION_URL" | sed -n 's|.*apache-maven-\([0-9.]*\)-bin\.tar\.gz|\1|p')
fi

if [ -z "$MAVEN_VERSION" ]; then
  echo "Unable to determine Maven version from distributionUrl=$DISTRIBUTION_URL" >&2
  exit 1
fi

MAVEN_HOME="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION"
ARCHIVE_PATH="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION-bin.tar.gz"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$WRAPPER_DIR"
  if [ ! -f "$ARCHIVE_PATH" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "$DISTRIBUTION_URL" -o "$ARCHIVE_PATH"
    elif command -v wget >/dev/null 2>&1; then
      wget -q "$DISTRIBUTION_URL" -O "$ARCHIVE_PATH"
    else
      echo "curl or wget is required to download Maven" >&2
      exit 1
    fi
  fi

  rm -rf "$MAVEN_HOME"
  tar -xzf "$ARCHIVE_PATH" -C "$WRAPPER_DIR"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
