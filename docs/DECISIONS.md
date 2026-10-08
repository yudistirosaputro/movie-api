# Decisions

One line of reasoning per decision. Grouped by area; "Deviation" marks places where the brief was not followed literally, and why.

## Tooling and project setup

| Decision | Reason |
|---|---|
| Scaffolded with `android create empty-activity --name="Movie API" --minSdk=24 --output=./movie-api`, then renamed the package to `com.yudistirosaputro.movieapi`. **Deviation**: the brief's `--application-id` flag doesn't exist in Android CLI 1.0.15498356. | Same template, just a manual rename afterwards. |
| `./movie-api` is its own git repository; the parent `movie-claude` folder was left untouched. | The GitHub repo is `movie-api`, so its root is the project folder. |
| Used the existing `claude_API36` AVD (API 36, x86_64) instead of creating a new API 35 image. | The brief says "use claude API36" for recordings; reusing the AVD avoids a multi-GB image download. |
| compileSdk / targetSdk **37**, minSdk 24. | API 37 is the newest stable platform (`android sdk list`), and the newest AGP 9.4.1 supports up to 37. |
| Every version comes from Maven Central / Google Maven metadata, looked up on 2026-10-08, and lives in `gradle/libs.versions.toml`. | The brief forbids versions from memory. |
| AGP 9.4.1 with **built-in Kotlin** (no `kotlin-android` plugin), Gradle 9.8.1, Kotlin 2.4.21, KSP 2.3.12, Hilt 2.60.1. | AGP 9 compiles Kotlin itself; the Kotlin plugins align KGP to 2.4.21 (verified with `buildEnvironment`). |
| dimock `0.1.0-alpha02` (`dimock-ui` debug, `dimock-okhttp-no-op` release). | The newest published artifact; there is no stable 0.1.0 on Maven Central yet. |
| `android skills add` for `jetpack-compose-m3`, `testing-setup`, `edge-to-edge` and `android-cli`. | Those match Compose, testing and edge-to-edge. The CLI installs skills into every agent folder it knows (Copilot, OpenCode, Trae, Windsurf, `~/.config/agents`); that's its default behaviour. |
| `.gitattributes` forces LF (CRLF for `.bat`), with binary rules for media. | Git on this machine has `autocrlf` on; `gradlew` must keep LF to run on macOS/Linux. |

## API key

| Decision | Reason |
|---|---|
| `MOVIE_API` is read with `providers.gradleProperty`, exposed via `buildConfigField`, and checked with `./gradlew checkMovieApiKey` (prints only present/missing). | As specified; configuration-cache friendly. |
| One pair of surrounding quotes is stripped, both in Gradle and in `TmdbCredential.from`. | The user-level property is written as `MOVIE_API="…"`. `.properties` files keep quotes literally, so TMDB got `"…"` and answered 401. Diagnosed with a throwaway Gradle init script that printed only the value's shape (length, quote/charset flags), never the value. |
| Disclosure: before the Gradle task existed, one `grep -q '^MOVIE_API='` ran against `~/.gradle/gradle.properties` and printed only "present/missing". Nothing else ever read that file. | Honesty about the "never open the file" rule; the value was never printed, copied or edited. |
| Credential detection: a value starting with `eyJ` is a v4 bearer token; anything else non-blank is a v3 key; blank is Missing. | Matches the brief. `toString()` is redacted so the value can't leak through logs. |
| A missing key throws `MissingApiKeyException : IOException` from the auth interceptor. | OkHttp delivers IOExceptions to the caller; anything else would crash its dispatcher thread. Dimock still works with no key, because its interceptor runs first. |
| `HttpLoggingInterceptor` (debug, BASIC) uses `redactQueryParams("api_key")`. | The auth interceptor runs before logging, so a v3 key would otherwise appear in logcat. |

## Architecture

| Decision | Reason |
|---|---|
| Use cases sit between ViewModels and the repository (`domain/usecase`). | The repo owner's Clean Architecture rules require ViewModels to depend on use cases; the brief lists only models and repository interfaces for `domain`, and this is a strict superset. |
| `PagingData` appears in the domain repository interface. | `paging-common` is pure Kotlin (KMP), so the domain stays platform-free. |
| Domain results are `Outcome<T>` (`Success` / `Failure(AppError)`); paging errors travel as `AppException(AppError)`. | No exceptions cross layer boundaries; Paging's `LoadResult.Error` needs a `Throwable`. |
| Error mapping: any IOException other than a timeout maps to `NoInternet`; any 4xx other than 401/404/429 maps to `Unknown(code)`. TMDB's `status_message` is parsed and kept on the error, but the UI shows its own copy. | Most bare IOExceptions are connectivity issues; the app's copy reads more naturally than TMDB's sentences. |
| `NotFound` shows **Back** instead of Retry. | Retrying a movie that doesn't exist can't succeed. |
| Type-safe **Navigation Compose 2.10.2** replaces the template's Navigation 3. | The brief asks for Navigation Compose; it also provides `SavedStateHandle.toRoute()` and the `AnimatedContentScope` that shared elements need. |
| Non-paged screens expose `StateFlow<UiState>` (Loading/Content/Empty/Error). Paged screens expose `Flow<PagingData>`, and the screen derives Loading/Content/Empty/Error with the pure, unit-tested `ListState.from(loadStates, itemCount)`. | Paging owns that state; mirroring it into a second StateFlow would duplicate it. |
| Loaded items always win: a failed refresh or append never replaces items already on screen. | The brief: full-screen error only when the first page fails. |
| `TmdbPagingSource` drops ids already seen in the current generation, and commits them only after a successful load. | TMDB re-ranks popularity between page requests; a duplicate key crashes `LazyVerticalGrid`. Committing late keeps a retried append from filtering out its own items. |
| Pagination stops at `min(total_pages, 500)`. `getRefreshKey` returns null (always refresh from page 1). | TMDB rejects pages > 500; its pages aren't stable enough to resume mid-list. |
| **Process-death scroll restore**: the first visible index is saved in `SavedStateHandle`; on recreation the refresh loads pages `1..index/20+2` in a single load (capped at 10 pages) before the grid composes. | A plain restart would load only page 1 and `LazyGridState` would clamp the restored index. Verified on device in video 05. |
| The details media area is pinned at the top, with content scrolling beneath it. | The player is never disposed by scrolling, which is how streaming apps behave. |
| The shared element is `sharedBounds` from the poster card to the details media area. The details route carries `title` + `posterUrl`, so the Loading state already has the image. | The transition runs during the Loading state, before details arrive. |

## UI

| Decision | Reason |
|---|---|
| Original branding: the wordmark reads "movie" in white and "api" in crimson `#DC2F3C`, set in Inter Black; the launcher icon is a play triangle in a ring. | No Netflix name, logo, font or brand red. |
| Inter is bundled as 5 static TTFs (OFL license in `docs/licenses`), not as downloadable fonts. | Works offline and on devices without Play services. |
| `material-icons-extended` 1.7.8 (its last release). | It has every icon needed; R8 strips the unused ones in release. |
| Genres look like a "Categories" menu: large centered names, grey turning white and Black-weight while pressed, with a faint crimson glow at the top. | Matches the brief's Categories style. |
| Top bars draw a black background whose alpha follows scroll; the state is read in the draw phase. | Smooth fade without recomposing on every frame. |
| The shimmer and press-scale animations read their values in draw/graphicsLayer only. | Animations don't recompose. |
| Poster cards without an image show the title instead. | A poster-only grid must stay usable when `poster_path` is null. |
| The grid has 3 columns below 600 dp and `width/150dp` columns above. | 3 columns on phones as specified; landscape and tablets get more. |
| Dates use the device locale's MEDIUM date format and time zone. | Readable, and localised for free. |

## Trailer player

| Decision | Reason |
|---|---|
| `YouTubePlayerView` is inflated from XML with `enableAutomaticInitialization="false"`. | A view built in code auto-initialises with default options, and calling `initialize` again then throws. |
| The player is bound to the back-stack entry's lifecycle, released on dispose, and recreated per video key. | Lifecycle-aware as required; switching videos is simple and leak-free. |
| No `configChanges` in the manifest; the playing video, position, play/pause and full screen live in `SavedStateHandle`, so after rotation or process death the player resumes where it was. | The rotation / process-death requirement is met for real rather than by side-stepping recreation. Pause events while the lifecycle is below STARTED are ignored, so a rotation-induced pause doesn't stop playback from resuming. |
| Full screen hides the system bars and requests sensor-landscape; Back exits full screen first. | Netflix-style full screen. The emulator's one-time "Viewing full screen" system prompt was pre-confirmed for recordings (`settings put secure immersive_mode_confirmations confirmed`). |
| The full-screen toggle lives in the details top bar, not on the video. In full screen the exit button sits in the screen corner, outside the 16:9 player on 20:9 phones. | Overlaid on the video it covered YouTube's own settings button. |
| Trailer choice: YouTube + Trailer; official first, then newest `published_at`; otherwise the same rule for teasers; otherwise none. Site comparison ignores case. | As specified. For *Superman* this picks TMDB's newest official trailer, which is an ASL version; the rule is followed literally. |
| On a player error the screen offers **Open in YouTube** (`ACTION_VIEW` on the watch URL). | As specified; falls back silently if nothing can handle the intent. |

## Testing and verification

| Decision | Reason |
|---|---|
| Fixtures in `app/src/test/resources/fixtures` are real TMDB responses captured with dimock (bodies only). | The brief; captures happen before the auth interceptor, so they never contain the credential. |
| ViewModel tests run on **Robolectric with an SDK 34 sandbox**. | `SavedStateHandle.toRoute()` decodes through `android.os.Bundle`, which is a stub in plain JVM tests. Robolectric's SDK 35+ sandboxes need Java 21, and the toolchain is 17. |
| Compose UI tests target stateless screens with fake states; the YouTube player is a slot that tests replace with a box. | No WebView or network in instrumented tests. |
| `connectedDebugAndroidTest` ran with `ANDROID_SERIAL=emulator-5554`. | A second emulator (`emulator-5556`, another session's) was connected; tests must not touch it. |
| The fake credentials in tests are deliberately not shaped like real ones (`fake-v3-key-for-tests`, `eyJfake.test.token`). | They would otherwise trip the secret-scan gate's own patterns. |

## dimock and evidence videos

| Decision | Reason |
|---|---|
| Every scenario is a YAML rule file in `docs/dimock-rules/`, applied with `npx dimock mock set <file>` and cleared with `mock clear`. **Deviation**: in CLI 0.1.0-alpha02, `mock add <file>` fails ("provide rules as a file path…") because it forwards `file` to a tool that only accepts `rule`/`text`; the `mock from --path … --method …` form in the README doesn't exist in this version either. | `mock set` resolves `bodyFile` relative to the rule file, and only one scenario is active at a time anyway. |
| **Fallback for recording**: `adb shell screenrecord` in back-to-back 175 s segments, touch markers from Android's `show_touches`, interactions still driven by Argent's MCP tools. ffmpeg then concatenates the segments, caps every still stretch at 1.5 s (`setpts` with `PREV_INPTS`/`PREV_OUTPTS`, the same idea as Argent's `trimStatic`) and encodes 720p H.264. | Argent's `screen-recording-start` failed: "ffmpeg was not found on PATH". The shared Argent tool server (started a day earlier by another session) has no ffmpeg on its PATH, and restarting it would have torn down another agent's emulator (`emulator-5556`). The brief lists exactly this fallback. |
| Taps while in landscape went through `adb shell input tap`. | Argent 0.24.0's `gesture-tap` mis-maps coordinates in landscape (taps landed but missed the target); in portrait it is accurate. |
| `POST_NOTIFICATIONS` is declared only in `src/debug/AndroidManifest.xml` and requested only in debug builds; granted with `pm grant` before recording. | dimock's inspector notification is a debug tool; release builds never ask. |
| The happy path uses *Avengers: Endgame* (59 reviews = 3 pages, with a trailer). | The first movies in Action have < 20 reviews, so they couldn't show reviews paging. |

## Release build

| Decision | Reason |
|---|---|
| R8 minify + resource shrinking on release; the APK is unsigned. | Realistic release config; signing keys never belong in a take-home repo. |
| Core library desugaring for `java.time` on minSdk 24. | Correct date parsing/formatting below API 26. |
