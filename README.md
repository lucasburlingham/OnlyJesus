# OnlyJesus

OnlyJesus is an Android Bible reader app built with Kotlin and Jetpack Compose.

This project has completely been written by AI. I do not claim right to the code, and I do not take any responsibility for it.

## License

This project is released under [The Unlicense](https://unlicense.org), which dedicates the software to the public domain.

See [/LICENSE](/LICENSE) for the full text.

## Build

From the repository root:

```bash
./gradlew assembleRelease
```

The release APK is generated at:

`app/build/outputs/apk/release/app-release.apk`

Note: the release build is signed with the debug key so it can be installed locally on a device. If you want a distributable production build, replace this with your own release signing config.
