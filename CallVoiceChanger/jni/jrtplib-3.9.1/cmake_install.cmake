# Install script for directory: /Users/Steve/code/jrtplib-3.9.1/src

# Set the install prefix
IF(NOT DEFINED CMAKE_INSTALL_PREFIX)
  SET(CMAKE_INSTALL_PREFIX "/usr/local")
ENDIF(NOT DEFINED CMAKE_INSTALL_PREFIX)
STRING(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
IF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  IF(BUILD_TYPE)
    STRING(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  ELSE(BUILD_TYPE)
    SET(CMAKE_INSTALL_CONFIG_NAME "")
  ENDIF(BUILD_TYPE)
  MESSAGE(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
ENDIF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)

# Set the component getting installed.
IF(NOT CMAKE_INSTALL_COMPONENT)
  IF(COMPONENT)
    MESSAGE(STATUS "Install component: \"${COMPONENT}\"")
    SET(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  ELSE(COMPONENT)
    SET(CMAKE_INSTALL_COMPONENT)
  ENDIF(COMPONENT)
ENDIF(NOT CMAKE_INSTALL_COMPONENT)

IF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  FILE(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include/jrtplib3" TYPE FILE FILES
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcpapppacket.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcpbyepacket.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcpcompoundpacket.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcpcompoundpacketbuilder.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcppacket.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcppacketbuilder.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcprrpacket.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcpscheduler.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcpsdesinfo.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcpsdespacket.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcpsrpacket.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtcpunknownpacket.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpaddress.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpcollisionlist.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpconfig.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpdebug.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpdefines.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtperrors.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtphashtable.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpinternalsourcedata.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpipv4address.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpipv4destination.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpipv6address.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpipv6destination.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpkeyhashtable.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtplibraryversion.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpmemorymanager.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpmemoryobject.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtppacket.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtppacketbuilder.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtppollthread.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtprandom.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtprandomrand48.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtprandomrands.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtprandomurandom.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtprawpacket.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpsession.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpsessionparams.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpsessionsources.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpsourcedata.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpsources.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpstructs.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtptimeutilities.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtptransmitter.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtptypes_win.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtptypes.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpudpv4transmitter.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpudpv6transmitter.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpbyteaddress.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/rtpexternaltransmitter.h"
    "/Users/Steve/code/jrtplib-3.9.1/src/extratransmitters/rtpfaketransmitter.h"
    )
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

IF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  list(APPEND CMAKE_ABSOLUTE_DESTINATION_FILES
   "/usr/local/lib/libjrtp.a")
  IF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(WARNING "ABSOLUTE path INSTALL DESTINATION : ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
  IF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(FATAL_ERROR "ABSOLUTE path INSTALL DESTINATION forbidden (by caller): ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
FILE(INSTALL DESTINATION "/usr/local/lib" TYPE STATIC_LIBRARY FILES "/Users/Steve/code/jrtplib-3.9.1/src/libjrtp.a")
  IF(EXISTS "$ENV{DESTDIR}/usr/local/lib/libjrtp.a" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}/usr/local/lib/libjrtp.a")
    EXECUTE_PROCESS(COMMAND "/usr/bin/ranlib" "$ENV{DESTDIR}/usr/local/lib/libjrtp.a")
  ENDIF()
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

IF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  list(APPEND CMAKE_ABSOLUTE_DESTINATION_FILES
   "/usr/local/lib/libjrtp.3.9.1.dylib;/usr/local/lib/libjrtp.dylib")
  IF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(WARNING "ABSOLUTE path INSTALL DESTINATION : ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
  IF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(FATAL_ERROR "ABSOLUTE path INSTALL DESTINATION forbidden (by caller): ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
FILE(INSTALL DESTINATION "/usr/local/lib" TYPE SHARED_LIBRARY FILES
    "/Users/Steve/code/jrtplib-3.9.1/src/libjrtp.3.9.1.dylib"
    "/Users/Steve/code/jrtplib-3.9.1/src/libjrtp.dylib"
    )
  FOREACH(file
      "$ENV{DESTDIR}/usr/local/lib/libjrtp.3.9.1.dylib"
      "$ENV{DESTDIR}/usr/local/lib/libjrtp.dylib"
      )
    IF(EXISTS "${file}" AND
       NOT IS_SYMLINK "${file}")
      EXECUTE_PROCESS(COMMAND "/usr/bin/install_name_tool"
        -id "libjrtp.3.9.1.dylib"
        "${file}")
      IF(CMAKE_INSTALL_DO_STRIP)
        EXECUTE_PROCESS(COMMAND "/usr/bin/strip" "${file}")
      ENDIF(CMAKE_INSTALL_DO_STRIP)
    ENDIF()
  ENDFOREACH()
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

