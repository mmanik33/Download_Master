import re

with open('app/src/main/java/com/example/model/MediaModel.kt', 'r') as f:
    content = f.read()

content = content.replace("val isAudioOnly: Boolean = false\n)", "val isAudioOnly: Boolean = false,\n    val directUrl: String? = null\n)")

with open('app/src/main/java/com/example/model/MediaModel.kt', 'w') as f:
    f.write(content)
