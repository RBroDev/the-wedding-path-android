# The Wedding Path: Native Android Event Planner

An intuitive, native Android application designed to streamline the multi-month wedding preparation process into a structured, user-centric mobile experience. By shifting away from complex corporate spreadsheets, this application introduces an organic visual pipeline to reduce administrative overload and keep couples methodically aligned.

### 📱 Core Engineering Design & Architecture
* **Architectural Pattern:** Architecture follows the official **Model-View-ViewModel (MVVM)** pattern to cleanly decouple backend relational logic from reactive UI presentation elements.
* **Data Persistence:** Utilizes a local **SQLite database schema** to ensure absolute data persistence across distinct device sessions.
* **Asynchronous Streams:** Leverages background database queries to observe live streams, reactively re-rendering UI states whenever data boundaries cross.
* **System Notification Engine:** Invokes Android's background `AlarmManager` system services to trigger broadcast receivers and deliver timely system tray notifications without requiring the active UI viewport to be open.

### 🎨 User-Centered Features
* **Winding Milestone Checklist:** An innovative vertical recycler view layout where core planning checkpoints transition dynamically from semi-translucent states to vibrant color fills upon complete database storage entry.
* **Granular Multi-Event Binding:** Allows a single high-level checklist task to cleanly map and append multiple sequential sub-events (e.g., initial consultation, site reviews, final execution timing).
* **Synchronized Chronological Agenda:** A dual-pane dashboard containing a native monthly grid view tethered via date change listeners to custom filtered, scrollable text agenda fields.
