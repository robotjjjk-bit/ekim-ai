cmake_minimum_required(VERSION 4.1.2)

project(test)

add_library(
    native-lib
    SHARED
    native_keys.cpp)