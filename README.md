# Expense Tracker Android App

A beautifully formatted, offline-first personal finance and expense tracking application built using modern Android development practices, **Jetpack Compose**, and **Room Database**.

---

## Key Features

- **Welcoming Bento Balance Dashboard:** A single-glance overview of your current monthly spending progress against a configured budget limit.
- **Interactive Multi-category Analytics:** Custom graphic data visualizers including:
  - An interactive **Donut Chart** representing proportional percentage shares per category.
  - A responsive **7-Day Weekly Bar Chart** visualizing day-by-day historic spends.
  - Highlighting cards for top spending categories and your historic maximum single-item spend.
- **Advanced Tracker Search & Filters:** Instantly search through transaction titles/descriptions and isolate visual groups utilizing dynamic Material 3 category chips.
- **Custom Categories & Profiles:** Flexibly add or edit existing categories with custom indicators, icons, and colors, and personalize your profile avatar directly from the settings interface.
- **Secure Persistence:** Powered by a clean Room Database implementation for secure, robust local offline transactions.

---

## Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Declarative UI) with Material Design 3 (M3) specifications
- **Architecture:** MVVM Design Pattern (Model-View-ViewModel Architecture)
- **Local Database:** Room SQLite Engine (using Kotlin Symbol Processing - KSP)
- **Asynchronous Flow:** Kotlin Coroutines & Flow API for reactive, non-blocking UI state updates
- **Dependency Management:** Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`libs.versions.toml`)
- **Testing Surface:** Robolectric (Local JVM Unit & Component Testing) and Roborazzi (Screenshot & Visual Regression Testing)

---

## Local Setup & Prerequisites

Before starting, make sure your development environment has the following prerequisites met:

### 1. Developer Requirements
- **Operating System:** Windows, macOS, or Linux.
- **Java Development Kit (JDK):** **JDK 17** or higher configured.
- **IDE:** **Android Studio Koala (or newer version)** is highly recommended.
- **Android SDK:** Setup SDK target level **Platform 34** or above.

---

## Step-by-Step Installation

### Step 1: Open the Project
1. Extract or clone this repository to a local directory on your developer machine.
2. Launch **Android Studio**.
3. Select **File > Open**, navigate to the directory where the repository is cloned, and click **OK**.

### Step 2: Configure JDK 17
1. In Android Studio, go to:
   - **macOS:** `Android Studio > Settings > Build, Execution, Deployment > Build Tools > Gradle`
   - **Windows/Linux:** `File > Settings > Build, Execution, Deployment > Build Tools > Gradle`
2. Set the **Gradle JDK** dropdown to use **JDK 17** (or standard compatible version).
3. Click **Apply** and let Android Studio perform initial indexing.

### Step 3: Gradle Sync
1. Click the **"Sync Project with Gradle Files"** elephant tool button in the top-right toolbar.
2. This is automated to download the Room Database compiler, Jetpack Compose Material 3 dependencies, Kotlin Symbol Processing (KSP) toolchain, and libraries as defined centrally in the project's version catalog.

---

## Running the Application

1. Connect a physical Android device via USB with **USB Debugging** enabled, or start an Android Virtual Device (AVD) from the **Device Manager** inside Android Studio.
2. Select the `:app` run configuration in the top selector.
3. Click the green **Run (Play button)** or run `Shift + F10`.
4. The system compile chain will compile structural assets, resolve SQLite Room migrations, and install the finished debug `.apk` on your target screen.

---

## Executing Local Unit & Visual Regression Tests

The workspace includes local Roborazzi component visual tests and Robolectric unit tests that can be run directly from your local terminal workspace without requiring active emulator resources:

- **To run standard local unit tests & Robolectric components:**
  ```bash
  gradle :app:testDebugUnitTest
  ```
- **To record direct screenshot reference baselines (Roborazzi Visual Regression):**
  ```bash
  gradle :app:recordRoborazziDebug
  ```
- **To verify current layout alignments against recorded screenshots:**
  ```bash
  gradle :app:verifyRoborazziDebug
  ```
  Reports are generated statically inside your local build directories: `/app/build/reports/tests/testDebugUnitTest/index.html`.
