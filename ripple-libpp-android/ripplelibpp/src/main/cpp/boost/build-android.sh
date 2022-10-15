#!/bin/bash

if [[ "$compiler" != "gcc" ]]; then
	echo "'gcc' is the only supported compilers, while '${compiler}' was specified"
	exit 1
fi
if [[ "$targetArch" != "armeabi" ]] && [[ "$targetArch" != "armeabi-v7a" ]] && [[ "$targetArch" != "arm64-v8a" ]] && [[ "$targetArch" != "x86" ]] && [[ "$targetArch" != "x86_64" ]] && [[ "$targetArch" != "mips" ]] && [[ "$targetArch" != "mips64" ]]; then
	echo "'armeabi', 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64', 'mips', 'mips64' are the only supported target architectures, while '${targetArch}' was specified"
	exit 1
fi
echo "Going to build Boost for ${targetOS}/${compiler}/${targetArch}"

# Verify environment
if [[ -z "$ANDROID_SDK" ]]; then
	echo "ANDROID_SDK is not set"
	exit 1
fi
if [[ ! -d "$ANDROID_SDK" ]]; then
	echo "ANDROID_SDK '${ANDROID_SDK}' is set incorrectly"
	exit 1
fi
export ANDROID_SDK_ROOT=$ANDROID_SDK
echo "Using ANDROID_SDK '${ANDROID_SDK}'"

if [[ -z "$ANDROID_NDK" ]]; then
	echo "ANDROID_NDK is not set"
	exit 1
fi
if [[ ! -d "$ANDROID_NDK" ]]; then
	echo "ANDROID_NDK '${ANDROID_NDK}' is set incorrectly"
	exit 1
fi
export ANDROID_NDK_ROOT=$ANDROID_NDK
echo "Using ANDROID_NDK '${ANDROID_NDK}'"

if [[ "$(uname -a)" =~ Linux ]]; then
	if [[ "$(uname -m)" == x86_64 ]] && [ -d "$ANDROID_NDK/prebuilt/linux-x86_64" ]; then
		export ANDROID_NDK_HOST=linux-x86_64
	elif [ -d "$ANDROID_NDK/prebuilt/linux-x86" ]; then
		export ANDROID_NDK_HOST=linux-x86
	else
		export ANDROID_NDK_HOST=linux
	fi

	if [[ -z "$CPU_CORES_NUM" ]]; then
		CPU_CORES_NUM=`nproc`
	fi
elif [[ "$(uname -a)" =~ Darwin ]]; then
	if [[ "$(uname -m)" == x86_64 ]] && [ -d "$ANDROID_NDK/prebuilt/darwin-x86_64" ]; then
		export ANDROID_NDK_HOST=darwin-x86_64
	elif [ -d "$ANDROID_NDK/prebuilt/darwin-x86" ]; then
		export ANDROID_NDK_HOST=darwin-x86
	else
		export ANDROID_NDK_HOST=darwin
	fi

	if [[ -z "$CPU_CORES_NUM" ]]; then
		CPU_CORES_NUM=`sysctl hw.ncpu | awk '{print $2}'`
	fi
else
	echo "'$(uname -a)' host is not supported"
	exit 1
fi
if [[ -z "$ANDROID_SDK" ]]; then
	echo "ANDROID_NDK '${ANDROID_NDK}' contains no valid host prebuilt tools"
	exit 1
fi
echo "Using ANDROID_NDK_HOST '${ANDROID_NDK_HOST}'"

export ANDROID_TARGET_ARCH=$targetArch
echo "Using ANDROID_TARGET_ARCH '${ANDROID_TARGET_ARCH}'"

# Configuration
BOOST_CONFIGURATION=$(echo "
	--layout=versioned
	--with-thread
	--with-regex
	toolset=gcc-android
	target-os=linux
	threading=multi
	link=static
	runtime-link=shared
	variant=release
	threadapi=pthread
	stage
" | tr '\n' ' ')

# Configure & Build static
STATIC_BUILD_PATH="$SRCLOC/upstream.patched.${targetOS}.${compiler}-${targetArch}.static"
if [ ! -d "$STATIC_BUILD_PATH" ]; then
	cp -rpf "$SRCLOC/upstream.patched" "$STATIC_BUILD_PATH"

	(cd "$STATIC_BUILD_PATH" && \
		./bootstrap.sh)
	retcode=$?
	if [ $retcode -ne 0 ]; then
		echo "Failed to configure 'Boost' for '${targetOS}.${compiler}-${targetArch}', aborting..."
		rm -rf "$path"
		exit $retcode
	fi
	
	echo "Using '${targetOS}.${compiler}-${targetArch}.jam'"
	cat "$SRCLOC/targets/${targetOS}.${compiler}-${targetArch}.jam" > "$STATIC_BUILD_PATH/project-config.jam"
fi
(cd "$STATIC_BUILD_PATH" && \
	./b2 $BOOST_CONFIGURATION -j $CPU_CORES_NUM)
retcode=$?
if [ $retcode -ne 0 ]; then
	echo "Failed to build 'Boost' for '${targetOS}.${compiler}-${targetArch}', aborting..."
	rm -rf "$path"
	exit $retcode
fi

exit 0
