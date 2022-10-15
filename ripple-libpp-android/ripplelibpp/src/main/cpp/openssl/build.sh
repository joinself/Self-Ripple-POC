#!/bin/bash

if [ -z "$BASH_VERSION" ]; then
	echo "Invalid shell, re-running using bash..."
	exec bash "$0" "$@"
	exit $?
fi
SRCLOC="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "$SRCLOC/../functions.sh"

# Cleanup environment
cleanupEnvironment

# Verify input
targetOS=$1
compiler=$2
targetArch=$3
if [[ "$targetOS" == "android" ]]; then
	source "$SRCLOC/build-android.sh"
	exit $?
else
	echo "'android' is the only supported target, while '${targetOS}' was specified"
	exit 1
fi
