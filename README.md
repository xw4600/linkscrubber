# LinkScrubber

Simple Android app to remove the tracking data from shared links.  It aims to be simple and frictionless.  Under the hood, it's a share-sheet target that strips the query string from a shared link and copies the cleaned URL to the clipboard.  From there, share the clean link with to any installed app.  Free like freedom, and in this case also like beer.

A few sites (YouTube, X, Amazon, Google Maps/Drive, Dropbox, Zoom) keep their identity parameters; see `app/src/main/java/com/linkscrubber/ScrubRules.java`.

## Build

Requires JDK 17 and the Android SDK (platform 34, build-tools 34.0.0). Paths default to `~/tools`.

    ./build.sh

Runs the scrub tests, then produces `build/linkscrubber.apk`.

## Install

    adb install -r build/linkscrubber.apk

Then share a link from any app and choose LinkScrubber (use "Edit" in the share sheet if it is not listed).
