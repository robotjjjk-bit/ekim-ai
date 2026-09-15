cmake_minimum_required(VERSION 3.22.1)

project(test)

add_library(
    native-lib
    SHARED
    native_keys.cpp)