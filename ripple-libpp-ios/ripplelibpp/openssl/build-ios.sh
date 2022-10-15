#!/bin/bash

if [[ "$compiler" != "clang" ]]; then
	echo "'clang' is the only supported compilers, while '${compiler}' was specified"
	exit 1
fi
if [[ "$targetArch" != "i386" ]] && [[ "$targetArch" != "x86_64" ]] && [[ "$targetArch" != "armv7" ]] && [[ "$targetArch" != "armv7s" ]] && [[ "$targetArch" != "arm64" ]]; then
	echo "'i386', 'x86_64', 'armv7', 'armv7s', 'arm64' are the only supported target architectures, while '${targetArch}' was specified"
	exit 1
fi
echo "Going to build OpenSSL for ${targetOS}/${compiler}/${targetArch}"

if [[ "$(uname -a)" =~ Linux ]]; then
	if [[ -z "$CPU_CORES_NUM" ]]; then
		CPU_CORES_NUM=`nproc`
	fi
elif [[ "$(uname -a)" =~ Darwin ]]; then
	if [[ -z "$CPU_CORES_NUM" ]]; then
		CPU_CORES_NUM=`sysctl hw.ncpu | awk '{print $2}'`
	fi
fi

TOOLCHAIN_PATH=$(dirname `xcodebuild -sdk iphoneos -find-executable clang++`)
TARGET=""
PLATFORM=""
SDK_VERSION="10.3"
if [[ "$targetArch" == "i386" ]]; then
	TARGET="ios-sim-cross-i386"
	PLATFORM="iPhoneSimulator"
elif [[ "$targetArch" == "x86_64" ]]; then
	TARGET="ios-sim-cross-x86_64"
	PLATFORM="iPhoneSimulator"
elif [[ "$targetArch" == "armv7" ]]; then
	TARGET="ios-cross-armv7"
	PLATFORM="iPhoneOS"
elif [[ "$targetArch" == "armv7s" ]]; then
	TARGET="ios-cross-armv7s"
	PLATFORM="iPhoneOS"
elif [[ "$targetArch" == "arm64" ]]; then
	TARGET="ios64-cross-arm64"
	PLATFORM="iPhoneOS"
fi
if [[ ! -d "$TOOLCHAIN_PATH" ]]; then
	echo "Toolchain at '$TOOLCHAIN_PATH' not found"
	exit 1
fi
echo "Using toolchain '${TOOLCHAIN_PATH}'"
echo "Using platform '${PLATFORM}'"

XCODE_ROOT=`xcode-select --print-path`
CROSS_TOP="${XCODE_ROOT}/Platforms/${PLATFORM}.platform/Developer"
CROSS_SDK="${PLATFORM}${SDK_VERSION}.sdk"

# Configure & Build static
STATIC_BUILD_PATH="$SRCLOC/upstream.patched.${targetOS}.${compiler}-${targetArch}.static"
if [ ! -d "$STATIC_BUILD_PATH" ]; then
	cp -rpf "$SRCLOC/upstream.patched" "$STATIC_BUILD_PATH"

	(cd "$STATIC_BUILD_PATH" && \
		PATH="$TOOLCHAIN_PATH/:$PATH" \
		CROSS_COMPILE="$TOOLCHAIN_PATH/" \
		CROSS_TOP="$CROSS_TOP" \
		CROSS_SDK="$CROSS_SDK" \
		./Configure $TARGET -static no-shared)
	retcode=$?
	if [ $retcode -ne 0 ]; then
		echo "Failed to configure 'OpenSSL' for '${targetOS}.${compiler}-${targetArch}', aborting..."
		rm -rf "$path"
		exit $retcode
	fi
fi
(cd "$STATIC_BUILD_PATH" && \
	PATH="$TOOLCHAIN_PATH/:$PATH" \
	CROSS_COMPILE="$TOOLCHAIN_PATH/" \
	CROSS_TOP="$CROSS_TOP" \
	CROSS_SDK="$CROSS_SDK" \
	make -j$CPU_CORES_NUM build_libs)
retcode=$?
if [ $retcode -ne 0 ]; then
	echo "Failed to build 'OpenSSL' for '${targetOS}.${compiler}-${targetArch}', aborting..."
	rm -rf "$path"
	exit $retcode
fi

exit 0
