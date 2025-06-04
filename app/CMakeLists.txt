cmake_minimum_required(VERSION 3.10)
project(filament_native)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra")

# Enable libc++_shared for Filament compatibility
set(CMAKE_ANDROID_STL_TYPE c++_shared)

# Filament is provided as precompiled libraries through Gradle
# No additional native compilation needed for this project