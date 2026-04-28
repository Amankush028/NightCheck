File tree

nightcheck/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── app/
│   └── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    ├── res/
    │   ├── values/
    │   │   └── strings.xml
    │   └── xml/
    │       ├── today_tasks_widget_info.xml
    │       └── quick_add_widget_info.xml
    └── java/com/nightcheck/
        ├── NightcheckApp.kt
        │
        ├── data/
        │   ├── local/
        │   │   ├── NightcheckDatabase.kt
        │   │   ├── dao/
        │   │   │   ├── TaskDao.kt
        │   │   │   ├── NoteDao.kt
        │   │   │   └── ReminderDao.kt
        │   │   └── entity/
        │   │       ├── TaskEntity.kt
        │   │       ├── NoteEntity.kt
        │   │       ├── ReminderEntity.kt
        │   │       └── EntityMappers.kt
        │   └── repository/
        │       ├── TaskRepositoryImpl.kt
        │       ├── NoteRepositoryImpl.kt
        │       └── ReminderRepositoryImpl.kt
        │
        ├── domain/
        │   ├── model/
        │   │   ├── Task.kt
        │   │   ├── Note.kt
        │   │   ├── Reminder.kt
        │   │   ├── Priority.kt
        │   │   └── TaskStatus.kt
        │   ├── repository/
        │   │   ├── TaskRepository.kt
        │   │   ├── NoteRepository.kt
        │   │   └── ReminderRepository.kt
        │   └── usecase/
        │       ├── GetTodayTasksUseCase.kt
        │       ├── GetPendingTodayTasksUseCase.kt
        │       ├── SaveTaskUseCase.kt
        │       ├── SaveNoteUseCase.kt
        │       ├── UpdateTaskStatusUseCase.kt
        │       ├── SnoozeTaskUseCase.kt
        │       └── DeleteTaskUseCase.kt
        │
        ├── di/
        │   ├── DatabaseModule.kt
        │   ├── RepositoryModule.kt
        │   └── DataStoreModule.kt
        │
        ├── notification/
        │   └── NotificationHelper.kt
        │
        ├── receiver/
        │   ├── ReminderReceiver.kt
        │   ├── EndOfDayReceiver.kt
        │   └── BootReceiver.kt
        │
        ├── worker/
        │   └── EndOfDaySchedulerWorker.kt
        │
        ├── util/
        │   ├── AlarmScheduler.kt
        │   └── PreferencesManager.kt
        │
        └── ui/
            ├── MainActivity.kt
            ├── theme/
            │   ├── NightcheckTheme.kt
            │   └── NightcheckTypography.kt
            ├── navigation/
            │   ├── Screen.kt
            │   ├── NightcheckNavGraph.kt
            │   └── NightcheckBottomBar.kt
            ├── components/
            │   └── Cards.kt
            ├── home/
            │   ├── HomeScreen.kt
            │   └── HomeViewModel.kt
            ├── tasks/
            │   ├── TasksScreen.kt
            │   └── TasksViewModel.kt
            ├── notes/
            │   ├── NotesScreen.kt
            │   └── NotesViewModel.kt
            ├── addedittask/
            │   ├── AddEditTaskScreen.kt
            │   └── AddEditTaskViewModel.kt
            ├── addeditnote/
            │   ├── AddEditNoteScreen.kt
            │   └── AddEditNoteViewModel.kt
            ├── review/
            │   ├── EndOfDayReviewActivity.kt
            │   └── EndOfDayReviewViewModel.kt
            ├── settings/
            │   ├── SettingsScreen.kt
            │   └── SettingsViewModel.kt
            └── widget/
                ├── TodayTasksWidget.kt
                ├── TodayTasksWidgetStateHelper.kt
                ├── QuickAddWidget.kt
                └── MarkTaskCompleteAction.kt
