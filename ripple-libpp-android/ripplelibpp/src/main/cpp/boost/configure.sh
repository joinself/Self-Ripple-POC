#!/bin/bash

if [ -z "$BASH_VERSION" ]; then
	echo "Invalid shell, re-running using bash..."
	exec bash "$0" "$@"
	exit $?
fi
SRCLOC="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "$SRCLOC/../functions.sh"

prepareUpstreamFromTarArchive "$SRCLOC" "https://dl.bintray.com/boostorg/release/1.64.0/source/boost_1_64_0.tar.bz2"
patchUpstream "$SRCLOC"
