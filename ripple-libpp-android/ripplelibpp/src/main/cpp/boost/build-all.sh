#!/bin/bash

if [ -z "$BASH_VERSION" ]; then
	echo "Invalid shell, re-running using bash..."
	exec bash "$0" "$@"
	exit $?
fi
SRCLOC="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

"$SRCLOC/build.sh" android gcc armeabi && \
	"$SRCLOC/build.sh" android gcc armeabi-v7a && \
	"$SRCLOC/build.sh" android gcc arm64-v8a && \
	"$SRCLOC/build.sh" android gcc x86 && \
	"$SRCLOC/build.sh" android gcc x86_64 && \
	"$SRCLOC/build.sh" android gcc mips && \
	"$SRCLOC/build.sh" android gcc mips64
