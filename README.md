# iunrar

WORK IN PROGRESS - DO NOT USE

iunrar (read "I unrar") is a simple application that unpacks RAR files.
It supports RAR version 4 and older.

## How to build application from sources

You need JDK 16 or newer to compile and run this application.
```
# This will add module-info to dependencies that are not fully
# JPMS compliant, like e.g. Guava.
./build-modularized-version-of-dependencies.sh

./mvnw clean package
./mvnw javafx:run -pl gui
```

To create a macOS application package:
```
./mvnw clean install -P mkinstaller
```

(OK it should work on JDK 11 too, but I am running all CI builds on JDK 16.)

