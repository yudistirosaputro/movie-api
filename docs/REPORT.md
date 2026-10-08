# Final report: Movie API

**Repository:** https://github.com/yudistirosaputro/movie-api (public)

Built unattended on 2026-10-08/09 (Windows 11, Android CLI 1.0.15498356, emulator `claude_API36` = Android 16 / API 36), against the live TMDB API.

## User stories

| Story | Status | Summary |
|---|---|---|
| US1 Genres | ✅ | "Categories"-style genre list (large centered names, press turns white/bold, wordmark, About button); skeleton / content / empty / error + Retry; tap opens US2. |
| US2 Discover | ✅ | 3-column 2:3 poster grid (`with_genres`, `popularity.desc`, `include_adult=false`); title fallback cards; fading top bar; poster → details shared-element transition. |
| US3 Details | ✅ | Pinned 16:9 backdrop with gradient + play icon; title, `year · runtime · ★ rating (votes)` + language badge, tagline, Play Trailer, overview ("No overview available" fallback), genres; each field degrades on its own; loading / error / not-found (Back). |
| US4 Reviews | ✅ | Reviews tab with the first 3 reviews + "See all reviews (N)"; avatar (initial fallback), author, rating chip, date, 6-line collapse with Read more / Show less; "No reviews yet"; paged reviews screen. |
| US5 Trailer | ✅ | Videos load independently; official-newest YouTube trailer → teaser → none ("Trailer not available"); inline player with full-screen toggle, resumes after rotation and process death; "Open in YouTube" on player error; Trailers & More plays any YouTube video in the media area. |
| US6 Endless scrolling | ✅ | Paging 3 for discover and reviews: prefetch, unique keys (cross-page duplicates dropped), stops at `total_pages`/500, red footer loader, footer Retry keeps loaded items, full-screen error only for page 1. |
| US7 Positive & negative cases | ✅ | All 15 rows below. |

## Tests

| Suite | Count | Result |
|---|---|---|
| `./gradlew testDebugUnitTest` (JVM + Robolectric) | 91 | ✅ 91 passed, 0 failed |
| `./gradlew connectedDebugAndroidTest` (Compose UI, emulator-5554) | 33 | ✅ 33 passed, 0 failed |
| `./gradlew lint` | 0 issues | ✅ |
| `./gradlew assembleDebug assembleRelease` | 24 MB debug / 2.9 MB release (R8, dimock no-op) | ✅ |

Unit tests cover the mappers (including null fields), error mapping (401/404/429/5xx/other, IOException, timeout, SerializationException, missing key), the auth interceptor (v3 query param vs v4 bearer vs missing), credential parsing, trailer selection, the repository against MockWebServer with real captured fixtures, the PagingSource via `TestPager` (refresh, append, end, 500 cap, failure, retry, dedupe, restore), use cases, ViewModels with Turbine, and UI mapping. Compose tests cover every screen's Loading / Content / Empty / Error, Retry and Back callbacks, Read more, Play Trailer hidden without a trailer, the player slot, and Open in YouTube.

## Case matrix

| # | Scenario | Status | Verified by |
|---|---|---|---|
| P1 | All endpoints succeed | ✅ | Live API, video 01; repository and UI tests |
| P2 | Multi-page scrolling | ✅ | Video 01 (discover pages 1-6, reviews 1-3, confirmed in dimock captures); `TmdbPagingSourceTest`; `asSnapshot` over captured pages |
| P3 | Trailer + reviews | ✅ | Video 01 (*Avengers: Endgame*, 59 reviews); `TrailerSelectorTest`, `DetailsViewModelTest` |
| N1 | No internet | ✅ | `svc wifi/data disable` → error → enable → Retry (video 02); `ErrorMapperTest`, `GenresViewModelTest` |
| N2 | Slow / timeout | ✅ | dimock 4 s delay (skeleton) + one-shot timeout → Retry (video 02); `ErrorMapperTest` |
| N3 | 401 | ✅ | dimock 401 with TMDB body (video 03); `ErrorMapperTest`, `MovieRepositoryImplTest`, `GenresScreenTest` |
| N4 | 404 | ✅ | dimock 404 on `/3/movie/*` → "Movie not found" + Back (video 03); repository + `DetailsScreenTest` |
| N5 | 429 / 503 | ✅ | dimock 429 (discover) and 503 (details) → Retry after clearing (video 02); `ErrorMapperTest`, `MoviesScreenTest` |
| N6 | Malformed JSON | ✅ | dimock `malformed_body` (video 03); repository (malformed + empty body), `DetailsScreenTest` |
| N7 | Empty genres / discover / reviews | ✅ | dimock empty bodies (video 03); VM + screen tests |
| N8 | No trailer | ✅ | dimock empty `/videos` (video 03); `TrailerSelectorTest`, `DetailsViewModelTest`, `DetailsScreenTest` |
| N9 | Null fields | ✅ | dimock bodies with null poster/backdrop/overview/runtime/date/avatar/rating (video 03); `MappersTest`, screen tests |
| N10 | Append failure | ✅ | dimock `page: "2"` → 500, `times: 1`: footer Retry, page 1 kept, then pages 2-4 load (video 04; captures show mocked 500 then real 200); `TmdbPagingSourceTest`, `MoviesScreenTest` |
| N11 | Missing `MOVIE_API` | ✅ | Unit tests (credential, interceptor, repository sends no request, VM, screen); Gradle warning + `checkMovieApiKey` |
| N12 | Rotation / process death | ✅ | Video 05: grid rotation keeps position; details tab + scroll + playing trailer survive rotation; HOME → `am kill` → relaunch restores details, tab and trailer position, and the grid reloads pages 1-4 to restore its deep position. `DetailsViewModelTest`, `PagedViewModelsTest`, `TmdbPagingSourceTest` |

## Evidence

- Videos: `docs/evidence/01-happy-path.mp4` (2:36), `02-network-failures.mp4` (1:14), `03-data-edge-cases.mp4` (1:13), `04-paging-append-failure.mp4` (0:38), `05-rotation-process-death.mp4` (2:14). All 720p H.264, 1.8–15.4 MB.
- Screenshots: `docs/screenshots/01-genres.png`, `02-movies-grid.png`, `03-details.png`, `04-trailer-and-videos.png`, `05-reviews.png`, `06-error-no-internet.png`, `07-append-error-footer.png`, `08-about.png`.
- dimock rules: `docs/dimock-rules/*.yaml` (+ `bodies/`); test fixtures captured with dimock in `app/src/test/resources/fixtures/`.

## Decisions (highlights; full list in [DECISIONS.md](DECISIONS.md))

- Clean Architecture with use cases between ViewModels and the repository; `Outcome<T>` / `AppError` across layers; Hilt DI; type-safe Navigation Compose.
- Paged lists derive their UI state from Paging's load states with a pure, unit-tested `ListState`; loaded items always win over later failures.
- Process-death scroll restore reloads the pages up to the saved index in one refresh; the PagingSource drops TMDB's cross-page duplicate ids.
- No `configChanges`: rotation and process death are handled with `SavedStateHandle` (including the player's video, position, play state and full screen).
- Original branding (crimson `#DC2F3C`, "movie**api**" wordmark, play-ring icon), bundled Inter, dark theme only.
- R8 on release, the dimock no-op artifact in release, `POST_NOTIFICATIONS` declared and requested only in debug.

## Deviations and fallbacks

1. **Android CLI has no `--application-id`**: scaffolded with the CLI's empty-activity template and renamed the package manually.
2. **AVD**: used the existing `claude_API36` (API 36) as the brief's "use claude API36" asks, instead of creating an API 35 image.
3. **`MOVIE_API` has surrounding quotes** in the user Gradle properties, so the build strips them (TMDB had answered 401 with the quoted value). Disclosure: before the Gradle check task existed, one `grep -q` on that file printed only present/missing; the value was never printed, copied or edited.
4. **Argent screen recording unavailable**: the shared Argent tool server has no ffmpeg on its PATH, and restarting it would have disrupted another session's emulator. Used the brief's fallback instead: `adb shell screenrecord` (chained segments), `show_touches` markers, Argent-driven interactions, and ffmpeg post-processing (still stretches capped at 1.5 s).
5. **Argent 0.24.0 `gesture-tap` misplaces taps in landscape**, so landscape taps used `adb shell input tap`.
6. **dimock CLI 0.1.0-alpha02**: `mock add <file>` is broken and `mock from --path` doesn't exist, so every scenario was applied with `mock set <file>`.
7. **ViewModel unit tests use Robolectric (SDK 34 sandbox)**, because `SavedStateHandle.toRoute()` needs a real `Bundle`.
8. Fake test credentials were made deliberately non-key-shaped, and local, unpushed history was rewritten (`git filter-branch`) to remove the earlier key-shaped placeholders before the secret-scan gate.

## Missing credentials

None. `MOVIE_API` was present (a v4 Read Access Token), `gh` is authenticated as `yudistirosaputro`, and the git identity was configured.

## Follow-ups for the user

- Argent 0.27.0 and Android CLI 1.0.16500706 updates are available (not applied without your consent). Argent 0.27 may fix the landscape tap mapping; restarting the shared Argent tool server from a shell that has ffmpeg on PATH would enable native `screen-recording-*`.
- An app named `com.example.movieapi` is installed on the emulator from an earlier run; it was left untouched.
