# Release guide

## 1. Versioning

| Field | Where | Current |
|---|---|---|
| `versionName` | `app/build.gradle.kts` (`apexVersionName` property or `1.0.0`) | 1.0.0 |
| `versionCode` | `app/build.gradle.kts` (`apexVersionCode` property or `1`) | 1 |
| Artifact name | `.github/workflows/release.yml` | `APEX-Calculator-v1.0.0-release.apk` |

Override without editing files:

```bash
./gradlew :app:assembleRelease -PapexVersionName=1.1.0 -PapexVersionCode=2
```

## 2. Signing

The release variant reads four environment variables:

| Variable | Meaning |
|---|---|
| `APEX_KEYSTORE_FILE` | Absolute path to the `.jks`/`.keystore` file |
| `APEX_KEYSTORE_PASSWORD` | Keystore password |
| `APEX_KEY_ALIAS` | Key alias |
| `APEX_KEY_PASSWORD` | Key password |

They are never committed. In CI they come from repository secrets:

```
gh secret set APEX_KEYSTORE_BASE64 < keystore.b64
gh secret set APEX_KEYSTORE_PASSWORD
gh secret set APEX_KEY_ALIAS
gh secret set APEX_KEY_PASSWORD
```

`release.yml` decodes `APEX_KEYSTORE_BASE64` into a temporary file and exports
`APEX_KEYSTORE_FILE` for Gradle. When the secrets are absent the release variant falls back to the
debug signing identity; such an APK is installable but **must not be published as the production
release**. This fallback is documented here and in the delivery report.

Generate a production keystore once and keep it outside the repository:

```bash
keytool -genkeypair -v -keystore apex-release.jks -alias apex \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass <store-password> -keypass <key-password> \
  -dname "CN=Shohan Khan, OU=APEX Calculator, O=APEX, L=Dhaka, C=BD"
```

## 3. Build and verify locally

```bash
export APEX_KEYSTORE_FILE="$PWD/apex-release.jks"
export APEX_KEYSTORE_PASSWORD=…
export APEX_KEY_ALIAS=apex
export APEX_KEY_PASSWORD=…

./gradlew :app:assembleRelease
$ANDROID_HOME/build-tools/35.0.0/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
$ANDROID_HOME/build-tools/35.0.0/aapt2 dump badging \
  app/build/outputs/apk/release/app-release.apk | head -5
```

Expected package: `com.shohankhan.apexcalculator`, version `1.0.0 (1)`.

## 4. Publishing

`.github/workflows/release.yml` runs on a `v*` tag or manually. It:

1. builds and tests the release variant,
2. renames the APK to `APEX-Calculator-v<version>-release.apk`,
3. verifies the package name, version and signature,
4. uploads the APK as a workflow artifact,
5. creates a GitHub Release with the APK attached when `contents: write` is available.

```bash
git tag -a v1.0.0 -m "APEX Calculator 1.0.0"
git push origin v1.0.0
```

## 5. Checklist before publishing

- [ ] `./gradlew :app:testDebugUnitTest` green
- [ ] `./gradlew :app:lintDebug` green
- [ ] Release build signed with the production key
- [ ] `apksigner verify` reports the expected certificate
- [ ] `aapt2 dump badging` reports the expected package, version and label
- [ ] Merged manifest has no `INTERNET` permission
- [ ] APK installs and launches on a physical device (Android 8–15)
- [ ] `docs/ACCEPTANCE_CHECKLIST.md` updated with the result
