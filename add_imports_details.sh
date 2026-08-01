#!/bin/bash
awk '/^package/{print "package com.example.ui.screens.settings"; next} /^import/{print; next} /^@Composable/{exit} {if ($0 != "") print}' app/src/main/java/com/example/ui/screens/SettingsScreen.kt > temp_details.kt
echo "import com.example.ui.screens.settings.*" >> temp_details.kt
cat app/src/main/java/com/example/ui/screens/settings/SettingsDetailScreens.kt >> temp_details.kt
mv temp_details.kt app/src/main/java/com/example/ui/screens/settings/SettingsDetailScreens.kt
