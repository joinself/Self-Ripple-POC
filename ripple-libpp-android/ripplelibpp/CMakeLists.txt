cmake_minimum_required(VERSION 3.4.1)

set(rippled_PATH "src/main/cpp/ripple-libpp/extras/rippled")

set(ripplelibpp_PATH "src/main/cpp/ripple-libpp")
file(GLOB_RECURSE ripplelibpp_headers RELATIVE ${CMAKE_CURRENT_SOURCE_DIR}
    "${ripplelibpp_PATH}/include/*.hpp")
file(GLOB_RECURSE ripplelibpp_src RELATIVE ${CMAKE_CURRENT_SOURCE_DIR}
    "${ripplelibpp_PATH}/src/unity/*.cpp")

set(boost_PATH "src/main/cpp/boost")
set(openssl_PATH "src/main/cpp/openssl")

add_library(ripplelibpp SHARED
    "src/main/cpp/ripplelibpp.cpp"
    "src/main/cpp/wPublicKey.cpp"
    "src/main/cpp/wSecretKey.cpp"
    "src/main/cpp/wSeed.cpp"
    "src/main/cpp/wAccountID.cpp"
    "src/main/cpp/wCurrency.cpp"
    "src/main/cpp/wIssue.cpp"
    "src/main/cpp/wSTAmount.cpp"
    "src/main/cpp/wSTTx.cpp"
    "src/main/cpp/wBlob.cpp"
    "src/main/cpp/wSerializer.cpp"
    "src/main/cpp/wBuffer.cpp"
    "src/main/cpp/wSlice.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/${rippled_PATH}/src/ripple/unity/ed25519_donna.c"
    ${ripplelibpp_headers}
    ${ripplelibpp_src})

target_compile_definitions(ripplelibpp PRIVATE
    BOOST_COROUTINES_NO_DEPRECATION_WARNING)

target_include_directories(ripplelibpp PRIVATE
    "${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/${ripplelibpp_PATH}/src/include"
    "${CMAKE_CURRENT_SOURCE_DIR}/${rippled_PATH}/src"
    "${CMAKE_CURRENT_SOURCE_DIR}/${rippled_PATH}/src/secp256k1"
    "${CMAKE_CURRENT_SOURCE_DIR}/${rippled_PATH}/src/ed25519-donna"
    "${CMAKE_CURRENT_SOURCE_DIR}/${rippled_PATH}/src/beast/include"
    "${CMAKE_CURRENT_SOURCE_DIR}/${boost_PATH}/upstream.patched"
    "${CMAKE_CURRENT_SOURCE_DIR}/${openssl_PATH}/upstream.patched/include")

find_library(log_LIBRARY log)

set(boost_components
    system
    atomic
    thread
    regex)
foreach(boost_component ${boost_components})
    add_library(boost_${boost_component} STATIC IMPORTED)
    set_target_properties(boost_${boost_component} PROPERTIES IMPORTED_LOCATION "${CMAKE_CURRENT_SOURCE_DIR}/${boost_PATH}/upstream.patched.android.gcc-${ANDROID_ABI}.static/stage/lib/libboost_${boost_component}-gcc-mt-1_64.a")
endforeach()

add_library(openssl STATIC IMPORTED)
set_target_properties(openssl PROPERTIES IMPORTED_LOCATION "${CMAKE_CURRENT_SOURCE_DIR}/${openssl_PATH}/upstream.patched.android.gcc-${ANDROID_ABI}.static/libcrypto.a")

target_link_libraries(ripplelibpp
    ${log_LIBRARY}
    openssl
    boost_thread
    boost_regex
    boost_system)
