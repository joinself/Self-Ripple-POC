# ripple-libpp-ios

Preconditions:

Currently, full-scale building is only supported on macOS.

You must have Xcode installed.
For installation, please download from here:
https://developer.apple.com/download/

1. Clone the repository using the following command:
```
git clone --recursive https://github.com/vpay365/ripple-libpp-ios
cd ripple-libpp-ios
```

Dependencies:
```
Boost
OpenSSL
Currently dependency building is only possible on macOS.
```

For building OpenSSL, go to the folder
```
./ripplelibpp/openssl/configure.sh
./ripplelibpp/openssl/build-all.sh
```

For building Boost, go to the folder
```
./ripplelibpp/boost/configure.sh
./ripplelibpp/boost/build-all.sh
```

Building demo project from Xcode: ripple-libpp-ios/ripple-libpp-ios.xcworkspace
