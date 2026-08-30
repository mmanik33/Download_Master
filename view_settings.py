with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "Default Quality" in line or "Rate us" in line or "developer" in line.lower():
        print(f"{i}: {line.strip()}")
