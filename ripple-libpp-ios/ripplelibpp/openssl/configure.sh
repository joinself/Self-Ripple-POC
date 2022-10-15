#!/bin/bash

if [ -z "$BASH_VERSION" ]; then
	echo "Invalid shell, re-running using bash..."
	exec bash "$0" "$@"
	exit $?
fi
SRCLOC="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "$SRCLOC/../functions.sh"

prepareUpstreamFromTarArchive "$SRCLOC" "https://www.openssl.org/source/openssl-1.1.0f.tar.gz"
patchUpstream "$SRCLOC"
