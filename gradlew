#!/bin/sh
# Optional local wrapper shim. GitHub Actions uses gradle/actions/setup-gradle with a pinned Gradle version.
exec gradle "$@"
