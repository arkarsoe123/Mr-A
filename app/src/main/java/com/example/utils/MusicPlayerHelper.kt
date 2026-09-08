package com.example.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import java.io.File
import java.io.FileOutputStream

class MusicPlayerHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playBase64Audio(base64Audio: String) {
        try {
            val audioBytes = Base64.decode(base64Audio, Base64.DEFAULT)
            val tempFile = File.createTempFile("temp_audio", ".mp3", context.cacheDir)
            tempFile.deleteOnExit()
            val fos = FileOutputStream(tempFile)
            fos.write(audioBytes)
            fos.close()

            stop()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
}
