# Implementation Plan: Notion Android Widget App

## Context

Building a greenfield Android app that provides a 3x4 home screen widget for managing a Notion-based to-do list. The app must be extensible to support other backends (Microsoft Calendar, Google Tasks, etc.) in the future. Single Notion account at a time. Uses Glance (Jetpack) for widget UI.

---

## Architecture: Clean Architecture with MVVM + Repository + Provider Strategy

### Why Not MVC
MVC couples View and Controller tightly. On Android, Activities already act as both, leading to god activities. MVVM is Google's recommended pattern, more testable, and separates concerns cleanly.

### Layer Diagram

```
PRESENTATION LAYER
  Glance Widget | Compose UI (Auth/Settings) + ViewModels
        |                         |
DOMAIN LAYER
  Use Cases (AddTask, CompleteTask, etc.)
  Domain Models (Task, TaskList)
  Repository Interfaces
        |
DATA LAYER
  TaskRepository (impl)
    TaskProvider interface
      NotionTaskProvider (impl)
      MicrosoftTaskProvider (future)
      GoogleTaskProvider (future)
  Local Cache (Room DB)
  Auth Manager
```

### Key Extensibility Point: TaskProvider Interface

```kotlin
interface TaskProvider {
    val providerType: ProviderType
    suspend fun fetchTasks(databaseId: String): Result<List<Task>>
    suspend fun addTask(task: Task): Result<Task>
    suspend fun updateTask(taskId: String, updates: TaskUpdate): Result<Task>
    suspend fun deleteTask(taskId: String): Result<Unit>
}
```

Adding a new backend = implementing this single interface. No changes to widget, ViewModel, or use cases.

---

## Package Structure

```
app/src/main/kotlin/com/notionwidgets/
  di/                          # Hilt modules
    AppModule.kt
    NetworkModule.kt
    DatabaseModule.kt
  domain/                      # Pure Kotlin, no Android deps
    model/
      Task.kt
      TaskUpdate.kt
      ProviderType.kt
    repository/
      TaskRepository.kt       # Interface
    usecase/
      GetTasksUseCase.kt
      AddTaskUseCase.kt
      CompleteTaskUseCase.kt
      DeleteTaskUseCase.kt
  data/                        # Data layer
    provider/
      TaskProvider.kt          # Interface for backends
      notion/
        NotionTaskProvider.kt
        NotionApiService.kt    # Retrofit interface
        NotionMapper.kt        # Notion API response -> domain model
    repository/
      TaskRepositoryImpl.kt
    local/
      AppDatabase.kt
      TaskDao.kt
      TaskEntity.kt
    auth/
      AuthManager.kt
      NotionAuthManager.kt
  ui/                          # Compose screens
    auth/
      LoginScreen.kt
      LoginViewModel.kt
    settings/
      SettingsScreen.kt
      SettingsViewModel.kt
    database/
      DatabaseSelectScreen.kt  # Pick which Notion DB to use
      DatabaseSelectViewModel.kt
    theme/
      Theme.kt
  widget/                      # Widget layer (Glance)
    TodoGlanceWidget.kt        # GlanceAppWidget
    TodoWidgetReceiver.kt      # GlanceAppWidgetReceiver
    TodoWidgetContent.kt       # Composable widget UI
    WidgetSyncWorker.kt        # WorkManager periodic sync
  NotionWidgetApp.kt           # Application class (Hilt entry point)
```

---

## Data Flow: Widget Tap -> Notion API -> Widget Update

1. User taps complete checkbox on Glance widget
2. Glance ActionCallback triggers (runs in coroutine scope)
3. ActionCallback calls CompleteTaskUseCase via Hilt
4. UseCase calls TaskRepository.completeTask(taskId)
5. Repository calls NotionTaskProvider.updateTask() -> Notion API
6. Repository updates local Room cache
7. ActionCallback calls GlanceAppWidget.update() to refresh widget
8. Widget recomposes from local cache data

Note: Glance simplifies the flow vs RemoteViews - no PendingIntents or BroadcastReceivers needed.

---

## Implementation Phases

### Phase 1: Project Scaffold + Notion Integration Setup
- Initialize Android project with Gradle Kotlin DSL
- Set up Hilt dependency injection
- Guide: Create Notion public integration at developers.notion.com
  - Set redirect URI for OAuth callback
  - Configure capabilities (read/write content)
  - Note client ID and client secret
- Implement Notion OAuth 2.0 flow
  - LoginScreen (Compose) opens Custom Chrome Tab
  - Handle callback URI in Activity intent filter
  - Exchange auth code for access token
  - Store token in EncryptedSharedPreferences
- Create basic app shell with Compose navigation (login -> database select -> settings)

Key dependencies: Hilt, Retrofit, OkHttp, EncryptedSharedPreferences, Jetpack Compose, Navigation, Glance

### Phase 2: Domain + Data Layer (Core Logic)
- Define domain models: Task, TaskUpdate, ProviderType
- Implement TaskProvider interface
- Build NotionTaskProvider with Retrofit (Notion API v1)
  - POST /v1/databases/{id}/query (fetch tasks)
  - POST /v1/pages (create task)
  - PATCH /v1/pages/{id} (update/complete/delete task)
  - GET /v1/search (list user databases for selector)
- Implement TaskRepositoryImpl with Room local cache
- Write use cases: GetTasks, AddTask, CompleteTask, DeleteTask
- Database selector screen (query user databases, let them pick one)

### Phase 3: Glance Widget (User-Facing Feature)
- Build 3x4 Glance widget using Compose-like syntax
  - LazyColumn for scrollable task list
  - Add button at top
  - Each row: task title + complete checkbox + delete button
- Implement ActionCallbacks for add/complete/delete
- WorkManager for periodic background sync (every 15 min)
- Widget configuration activity (pick Notion database on widget add)

### Phase 4: Polish + Settings
- Settings screen: logout, select database, sync interval
- Error handling: offline state, API errors, token expiry with auto-refresh
- Loading states in widget
- Edge cases: empty list, long task names (truncation)

### Phase 5: Testing + Hardening
- Unit tests for use cases and repository
- Integration tests for Notion API calls (mock server)
- ProGuard/R8 rules for release build

---

## Key Technical Decisions

| Decision | Choice | Rationale |
|----------|--------|----------|
| Widget framework | Glance (Jetpack) | Compose-like syntax, cleaner than RemoteViews |
| DI | Hilt | Standard for Android, works with WorkManager |
| Networking | Retrofit + OkHttp | Industry standard, auth interceptors |
| Local cache | Room | Required for widget offline access |
| Background sync | WorkManager | Survives app kill, battery-friendly |
| Token storage | EncryptedSharedPreferences | Secure, AndroidX built-in |
| Auth flow | OAuth 2.0 + Custom Chrome Tab | Notion required flow for public integrations |
| Accounts | Single account | Simpler, can add multi-account later |

---

## Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| Notion API rate limits (3 req/sec) | Medium | Local cache + debounce widget taps |
| Glance API maturity | Medium | Fallback to RemoteViews if blocking issues found |
| Token expiry in background | Low | OkHttp Authenticator auto-refresh |
| Notion DB schema varies per user | Medium | Database selector + schema validation |
| WorkManager min interval 15 min | Low | Immediate sync on user action, periodic as backup |

---

## Verification

1. Auth: OAuth login -> token persisted -> survives app restart
2. Read: Widget displays tasks from selected Notion database
3. Add: New task appears in widget AND Notion
4. Complete: Checkbox marks done in widget AND Notion
5. Delete: Task removed from widget AND Notion
6. Offline: Cached data shown, syncs on reconnect
7. Extensibility: Mock TestTaskProvider needs zero changes outside data/provider/

---

## Task Tracking

### Phase 1+2: Project Scaffold + Domain + Data Layer

- [x] Task 1 (IN PROGRESS): Create Gradle build files and project structure
  - Created: settings.gradle.kts, root build.gradle.kts, gradle.properties, .gitignore
  - TODO: app/build.gradle.kts, gradle wrapper, AndroidManifest.xml
- [ ] Task 2: Create domain layer (models, repository interfaces, use cases)
  - Task.kt, TaskUpdate.kt, ProviderType.kt, TaskRepository interface
  - GetTasksUseCase, AddTaskUseCase, CompleteTaskUseCase, DeleteTaskUseCase
- [ ] Task 3: Create data layer (provider, Notion API, Room, repository impl)
  - TaskProvider interface, NotionTaskProvider, NotionApiService, NotionMapper
  - Room: AppDatabase, TaskDao, TaskEntity
  - TaskRepositoryImpl, AuthManager, NotionAuthManager
- [ ] Task 4: Create DI modules (Hilt)
  - AppModule.kt, NetworkModule.kt, DatabaseModule.kt
- [ ] Task 5: Create UI layer (Auth, Settings, Database Select screens)
  - LoginScreen + ViewModel, SettingsScreen + ViewModel, DatabaseSelectScreen + ViewModel
  - Theme.kt, Compose Navigation, MainActivity
- [ ] Task 6: Create Glance widget
  - TodoGlanceWidget, TodoWidgetReceiver, TodoWidgetContent
  - WidgetSyncWorker (WorkManager), widget metadata XML
- [ ] Task 7: Create Application class and AndroidManifest
  - NotionWidgetApp.kt (@HiltAndroidApp), complete AndroidManifest.xml
