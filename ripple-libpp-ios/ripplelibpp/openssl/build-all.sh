#!/bin/bash

if [ -z "$BASH_VERSION" ]; then
	echo "Invalid shell, re-running using bash..."
	exec bash "$0" "$@"
	exit $?
fi
SRCLOC="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

"$SRCLOC/build.sh" ios clang i386 && \
	"$SRCLOC/build.sh" ios clang x86_64 && \
	"$SRCLOC/build.sh" ios clang armv7 && \
	"$SRCLOC/build.sh" ios clang armv7s && \
	"$SRCLOC/build.sh" ios clang arm64

lipo \
	-create \
		"$SRCLOC/upstream.patched.ios.clang-i386.static/libcrypto.a" \
		"$SRCLOC/upstream.patched.ios.clang-x86_64.static/libcrypto.a" \
		"$SRCLOC/upstream.patched.ios.clang-armv7.static/libcrypto.a" \
		"$SRCLOC/upstream.patched.ios.clang-armv7s.static/libcrypto.a" \
		"$SRCLOC/upstream.patched.ios.clang-arm64.static/libcrypto.a" \
	-output \
		"$SRCLOC/upstream.patched/libcrypto.a"
