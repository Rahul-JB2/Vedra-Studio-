#!/bin/bash
awk '
/private fun PreferenceSectionHeader/ { flag = 1 }
flag == 1 { print $0 }
' app/src/main/java/com/example/ui/screens/SettingsScreen.kt > app/src/main/java/com/example/ui/screens/settings/SettingsComponents.kt
