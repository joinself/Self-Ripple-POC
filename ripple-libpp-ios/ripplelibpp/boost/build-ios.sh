#!/bin/bash

if [[ "$compiler" != "clang" ]]; then
	echo "'clang' is the only supported compilers, while '${compiler}' was specified"
	exit 1
fi
if [[ "$targetArch" != "i386" ]] && [[ "$targetArch" != "x86_64" ]] && [[ "$targetArch" != "armv7" ]] && [[ "$targetArch" != "armv7s" ]] && [[ "$targetArch" != "arm64" ]]; then
	echo "'i386', 'x86_64', 'armv7', 'armv7s', 'arm64' are the only supported target architectures, while '${targetArch}' was specified"
	exit 1
fi
echo "Going to build Boost for ${targetOS}/${compiler}/${targetArch}"

if [[ "$(uname -a)" =~ Linux ]]; then
	if [[ -z "$CPU_CORES_NUM" ]]; then
		CPU_CORES_NUM=`nproc`
	fi
elif [[ "$(uname -a)" =~ Darwin ]]; then
	if [[ -z "$CPU_CORES_NUM" ]]; then
		CPU_CORES_NUM=`sysctl hw.ncpu | awk '{print $2}'`
	fi
fi

# Configuration
BOOST_CONFIGURATION=$(echo "
	--layout=versioned
	--with-thread
	--with-regex
	toolset=clang-ios
	target-os=darwin
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
