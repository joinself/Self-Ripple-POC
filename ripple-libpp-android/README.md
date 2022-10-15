# ripple-libpp-android

## Preconditions:

Currently, full-scale building is only supported on Linux.

You must have Java, Android Sdk and Ndk installed.
For Java installation, please follow the instructions:
http://openjdk.java.net/install/

Android Sdk is a part of Android Studio
If there�s no Android Studio in the system, please download android studio from here: https://developer.android.com/studio/index.html and follow instructions from site

1. Clone the repository using the following command:
    git clone --recursive https://github.com/vpay365/ripple-libpp-android.git
    cd ripple-libpp-android

## Dependencies:

* Boost
* OpenSSL

Currently dependency building is only possible on Linux.

Before building the dependencies you should specify paths to Android Sdk and Ndk in the environment variables:
To check if the paths for Sdk and Ndk are set, run:
```
echo $ANDROID_SDK
echo $ANDROID_NDK
```

If these variables are set, the paths to Sdk and Ndk will be displayed.
If these variables are NOT set, please run these commands:
```
export ANDROID_SDK=path to android Sdk (for example, export ANDROID_SDK=~/Android/Sdk)
export ANDROID_NDK=path to android Ndk (for example, export ANDROID_NDK=~/Android/Ndk)
```

If the paths to Sdk and Ndk are unknown, you can check those via Android Studio:
Run Android Studio.
- If the Welcome window is displayed, choose Configure->Project Defaults->Project Structure->Sdk Location
You�ll see a window with Android SDK Location and Android NDK Location
- If a project is displayed, choose File->Project Structure->Sdk Location
You�ll see a window with Android SDK Location and Android NDK Location

For building OpenSSL, run
```
./ripplelibpp/src/main/cpp/openssl/configure.sh
./ripplelibpp/src/main/cpp/openssl/build-all.sh
```
        
For building Boost, run
```
./ripplelibpp/src/main/cpp/boost/configure.sh
./ripplelibpp/src/main/cpp/boost/build-all.sh
```
  
Building demo project from command line:
```
./gradlew build
```

