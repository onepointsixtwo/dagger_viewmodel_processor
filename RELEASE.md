# Release

## Pre-Requisites

The pre-requisites for releasing this are as follows:

1. The GPG key must be set (this is backed up).

2. There must be a global or local gradle configuration setup with the values ossrhUser, ossrhPassword for your sonatype username and password.


## Upload

Simply run the command ./gradlew uploadArchives to upload the built jar to Nexus, and then run
./gradlew closeAndReleaseRepository to actually close it and release it to maven central.

If you wish you can login to Sonatype between this two stages and look at Staging Repositories to
check that the repository is created as expected.
