import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

perm_old = '''    // Notification Permission for background downloads
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications allow real-time download progress.")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }'''

perm_new = '''    var isPermissionDone by remember { mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) }

    // Notification Permission for background downloads
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isPermissionDone = true
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications allow real-time download progress.")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                isPermissionDone = true
            }
        } else {
            isPermissionDone = true
        }
    }'''

content = content.replace(perm_old, perm_new)
content = content.replace('if (!hasAcceptedWarning) {', 'if (isPermissionDone && !hasAcceptedWarning) {')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
