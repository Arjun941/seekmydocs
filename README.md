# FindMyDoc

**FindMyDoc** is an offline, privacy-first document search engine for Android. It indexes documents already on your device — PDFs, Office files, text, and more — and lets you find them instantly with filename, keyword, OCR, or on-device semantic search. No document content ever needs to leave the device to be searchable.

> Built with Kotlin, Jetpack Compose, and on-device ML (TensorFlow Lite). Originally scaffolded via Google AI Studio.

## Features

- **Automatic background indexing** of documents on external storage (Downloads, Documents, MediaStore) via WorkManager, running every ~4 hours under battery/idle constraints
- **Real-time re-indexing** while the app is open, using a `ContentObserver` on the MediaStore
- **Incremental indexing** — files are re-indexed only when their name/size/modified-time hash changes
- **Hybrid search** combining:
  - Filename matching
  - Keyword / full-text search over extracted content
  - OCR text search (for scanned PDFs/images)
  - On-device semantic (vector) search using cosine similarity over chunk embeddings from a local Gemma embedding model
- **Ranked results** using a weighted score combining relevance, recency, and how often a document has been opened
- **Sandbox/demo mode** that generates sample documents (resume, bill, timetable, etc.) so search can be tried without real files
- **Open/share documents** directly from search results via a `FileProvider`
- **Dashboard** showing indexing stats: documents indexed, chunks, embeddings generated, OCR pages cached, storage used, last sync time
- **Settings** to toggle auto-indexing, OCR, semantic search, embedding engine, and dark mode
- Optional **Gemini / Firebase AI** integration for cloud-assisted features, gated behind a user-supplied API key

### Supported document formats

PDF, DOC/DOCX, XLS/XLSX, PPT/PPTX, TXT, CSV, MD, JSON, XML, EPUB, ODT/ODS/ODP

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM (`ViewModel` + `StateFlow` → Compose UI) |
| Local storage | Room |
| Background work | WorkManager |
| Networking | Retrofit, Moshi, OkHttp |
| On-device embeddings | TensorFlow Lite (Gemma embedding model, downloaded from Hugging Face on first run) |
| OCR | ML Kit Text Recognition *(currently stubbed — see [Known Limitations](#known-limitations))* |
| Cloud AI (optional) | Firebase AI / Gemini API, Firebase App Check |
| Testing | JUnit, Robolectric, Espresso, Roborazzi (screenshot tests) |

## Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt
├── core/
│   ├── embeddings/   # On-device embedding engines (Gemma / TFLite)
│   ├── extraction/   # Document text extraction
│   ├── indexing/     # Background & on-demand indexing workers, chunking, MediaStore observer
│   ├── ocr/          # OCR engine
│   └── search/       # Search pipeline & ranking
├── data/
│   ├── database/     # Room entities, DAO, database
│   └── repository/   # Indexing repository
├── presentation/
│   ├── MainViewModel.kt
│   └── ui/           # Compose screens
└── ui/theme/         # Compose theming
```

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (recent stable version)
- A physical device or emulator running **Android 7.0 (API 24)** or higher
- (Optional) A [Gemini API key](https://ai.google.dev/) for cloud AI features

### Setup

1. Clone the repository and open it in Android Studio.
2. Let Android Studio sync Gradle and resolve any suggested fixes.
3. Create a `.env` file in the project root (see `.env.example`) and set your key:
   ```
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   This is optional — the app builds and runs without it, but cloud AI features via Firebase/Gemini will be unavailable.
4. If you plan to use Firebase-backed features, add your own `google-services.json` to `app/`. The build does not fail without one (`googleServices.missing.passthrough = true`), but Firebase AI calls will not work.
5. For debug builds, Android Studio will generate/use a local `debug.keystore` automatically.
6. Run the app on an emulator or physical device.

On first launch, the app downloads a small Gemma embedding model (and tokenizer) from Hugging Face to enable semantic search — an internet connection is required at least once for this.

### Permissions

The app requests:
- `READ_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE` — to scan documents across device storage
- `INTERNET` / `ACCESS_NETWORK_STATE` — to download the embedding model and (optionally) call Gemini/Firebase

## Building a Release

Release builds are signed using a keystore referenced by environment variables in `app/build.gradle.kts`:

- `KEYSTORE_PATH` (defaults to `${rootDir}/my-upload-key.jks`)
- `STORE_PASSWORD`
- `KEY_PASSWORD`

Supply your own keystore and credentials before building a release APK/AAB — none are committed to the repository.

## Known Limitations

- **OCR is currently stubbed**: `LocalOcrEngine` returns simulated placeholder text rather than calling ML Kit, even though the ML Kit Text Recognition dependency is included.
- **ONNX Runtime** is included as a dependency but not currently used by the embedding pipeline (TensorFlow Lite is used instead) — likely reserved for a future/alternate model backend.
- Several dependencies (CameraX, Coil, Accompanist Permissions, DataStore Preferences, Play Services Location) are declared but commented out, suggesting planned-but-unbuilt features (e.g. camera-based capture, location tagging).
- No Gradle wrapper, CI configuration, or LICENSE file is currently included in the repository.

## License

No license has been specified for this project yet.
