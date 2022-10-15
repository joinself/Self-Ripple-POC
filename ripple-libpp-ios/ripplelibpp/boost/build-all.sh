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

BOOST_LIBRARIES=(system thread regex)
for BOOST_LIBRARY in "${BOOST_LIBRARIES[@]}"
do
	lipo \
		-create \
			"$SRCLOC/upstream.patched.ios.clang-i386.static/stage/lib/libboost_${BOOST_LIBRARY}-clang-darwin-mt-1_64.a" \
			"$SRCLOC/upstream.patched.ios.clang-x86_64.static/stage/lib/libboost_${BOOST_LIBRARY}-clang-darwin-mt-1_64.a" \
			"$SRCLOC/upstream.patched.ios.clang-armv7.static/stage/lib/libboost_${BOOST_LIBRARY}-clang-darwin-mt-1_64.a" \
			"$SRCLOC/upstream.patched.ios.clang-armv7s.static/stage/lib/libboost_${BOOST_LIBRARY}-clang-darwin-mt-1_64.a" \
			"$SRCLOC/upstream.patched.ios.clang-arm64.static/stage/lib/libboost_${BOOST_LIBRARY}-clang-darwin-mt-1_64.a" \
		-output \
			"$SRCLOC/upstream.patched/libboost_${BOOST_LIBRARY}-clang-mt-1_64.a"
done
