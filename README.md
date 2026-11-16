# TiemePlan

A planning to create timetables

Planned features:
- [ ] hard constrains
  - eg classroom can only be used for math
  - eg certain subject can only be given on Wednesday
- [ ] soft constraints
  - eg teachers not wanting to work Friday afternoon
  - eg math for certain classes in blocks of 2 hours
- [ ] schedule optimizations
  - eg as less as possible gap hours for teachers

## Downloads and releases

Every download bundles its own Java runtime, so users do not need Java installed.
jpackage can only build for the operating system it runs on.

| Platform | Download | How to run |
|----------|----------|------------|
| Linux (x64) | `Tiemeplan-<version>-linux-x64.tar.gz` | Extract anywhere and run `Tiemeplan/bin/Tiemeplan` (requires GTK 3, present on any desktop distribution) |
| Windows (x64) | `Tiemeplan-<version>-windows-x64.zip` | Unzip anywhere and run `Tiemeplan\Tiemeplan.exe` |
| macOS (Apple silicon) | `Tiemeplan-<version>-macos-arm64.zip` | Unzip and move `Tiemeplan.app` to Applications |

The Windows and macOS downloads are not signed yet:

- Windows shows a SmartScreen warning: choose **More info** and then **Run anyway**.
- macOS blocks the first start: open **System Settings > Privacy & Security** and choose **Open Anyway**,
  or run `xattr -dr com.apple.quarantine Tiemeplan.app`.

Build locally (requires JDK 26):

```bash
mvn clean package -Papp-image
```

The application image is written to `target/app-image/dist`.
On macOS add `-Djpackage.app.version=1.0.0` while the project version is below 1.0.0, because macOS rejects versions starting with 0.

To publish a release, run the **Release new version** workflow from the GitHub Actions tab and pick the type of release.
It tests and packages the application, bumps the version in `pom.xml`, tags it, and creates a GitHub release with a changelog and the downloads.
