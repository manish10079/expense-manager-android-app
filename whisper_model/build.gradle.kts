import java.io.FileOutputStream
import java.net.URI

plugins {
    alias(libs.plugins.android.asset.pack)
}

assetPack {
    packName.set("whisper_model")
    dynamicDelivery {
        deliveryType.set("fast-follow")
    }
}

val downloadWhisperModel by tasks.registering {
    val assetsDir = file("src/main/assets/whisper")
    outputs.dir(assetsDir)
    doLast {
        assetsDir.mkdirs()
        val modelFiles = listOf(
            "tiny-tokens.txt" to "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-tokens.txt",
            "tiny-encoder.int8.onnx" to "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-encoder.int8.onnx",
            "tiny-decoder.int8.onnx" to "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-decoder.int8.onnx"
        )
        for ((name, urlStr) in modelFiles) {
            val dest = file("$assetsDir/$name")
            if (!dest.exists() || dest.length() < 1000) {
                println("Downloading $name into asset pack...")
                URI(urlStr).toURL().openStream().use { input ->
                    FileOutputStream(dest).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}

tasks.matching { it.name.startsWith("package") || it.name.startsWith("process") || it.name.startsWith("bundle") }.configureEach {
    dependsOn(downloadWhisperModel)
}
