# Pratilipi Assignment

A simple Android app for creating and editing rich-text documents with local persistence.

## Features

- **Rich text editing**: Bold, italic, underline, strikethrough, text color, highlight, and inline images
- **Document list**: Create, open, and delete documents from a main list
- **Persistence**: All documents are stored locally using Room
- **Permissions**: Image insertion uses `READ_MEDIA_IMAGES` (API 33+) or `READ_EXTERNAL_STORAGE` (older devices) when needed

## Tech Stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| Min SDK | 23 |
| Architecture | MVVM |
| DI | Hilt |
| Database | Room |
| UI | ViewBinding, XML layouts, Material components |
| Async / UI updates | Kotlin Coroutines, LiveData |

## Project Structure

```
app/src/main/java/com/example/pratilipiassignment/
├── MainActivity.kt              # Document list screen
├── EditorActivity.kt            # Rich text editor screen
├── PratilipiApp.kt              # Application class
├── data/
│   ├── local/                   # Room entity, DAO, database
│   ├── model/                   # Document data class
│   └── repository/               # DocumentRepository
├── di/                          # Hilt modules
├── ui/
│   ├── editor/                  # EditorViewModel
│   └── list/                    # DocumentListViewModel, DocumentListAdapter
└── util/                        # RichTextConverter (HTML ↔ Spannable)
```

## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/PratilipiAssignment.git
   cd PratilipiAssignment
   ```

2. **Open in Android Studio**  
   Use *File → Open* and select the project folder. Let Gradle sync finish.

3. **Run**  
   Connect a device or start an emulator (API 23+), then run the app (e.g. **Run → Run 'app'**).

## Build

- **Debug**: `./gradlew assembleDebug`
- **Release**: `./gradlew assembleRelease` (signing must be configured for installable release builds)

## Requirements

- Android Studio (recommended) or command-line build with JDK 11+
- Android SDK with compileSdk 34
- Gradle 8.5 (included via wrapper)

## License

This project is for assignment purposes.
