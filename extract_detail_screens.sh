#!/bin/bash
awk '
/fun GeneralPreferencesDetailScreen/ { flag = 1 }
flag == 1 { print $0 }
' app/src/main/java/com/example/ui/screens/SettingsScreen.kt > app/src/main/java/com/example/ui/screens/settings/SettingsDetailScreens.kt
