package com.example.crazyapp

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Mp3PlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var handler: Handler
    private lateinit var audioList: MutableList<String>
    private var currentSongIndex = 0
    private val REQUEST_PERMISSION_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3_player)

        mediaPlayer = MediaPlayer()
        handler = Handler()
        audioList = mutableListOf()

        if (checkPermission()) {
            loadAudioFiles()
            setupUI()
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadAudioFiles()
            setupUI()
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAudioFiles() {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE)
        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                if (path.endsWith(".mp3")) {
                    audioList.add(path)
                }
            }
        }
        cursor?.close()

        if (audioList.isEmpty()) {
            Toast.makeText(this, "No MP3 files found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI() {
        val playPauseButton = findViewById<Button>(R.id.button_play_pause)
        val prevButton = findViewById<Button>(R.id.button_prev)
        val nextButton = findViewById<Button>(R.id.button_next)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val volumeSeekBar = findViewById<SeekBar>(R.id.volumeSeekBar)
        val nowPlayingText = findViewById<TextView>(R.id.text_now_playing)
        val loopButton = findViewById<Button>(R.id.button_loop)

        val updateSeekBar = object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    seekBar.progress = mediaPlayer.currentPosition
                    handler.postDelayed(this, 500)
                }
            }
        }

        fun playSong(index: Int) {
            if (audioList.isEmpty()) return

            mediaPlayer.reset()
            mediaPlayer.setDataSource(audioList[index])
            mediaPlayer.prepare()

            seekBar.max = mediaPlayer.duration
            nowPlayingText.text = "Playing: ${audioList[index].substringAfterLast("/")}"
            handler.post(updateSeekBar)
        }

        playPauseButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.text = "Play"
            } else {
                mediaPlayer.start()
                playPauseButton.text = "Pause"
                handler.post(updateSeekBar)
            }
        }

        prevButton.setOnClickListener {
            currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else audioList.size - 1
            playSong(currentSongIndex)
        }

        nextButton.setOnClickListener {
            currentSongIndex = if (currentSongIndex < audioList.size - 1) currentSongIndex + 1 else 0
            playSong(currentSongIndex)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                mediaPlayer.setVolume(volume, volume)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        loopButton.setOnClickListener {
            mediaPlayer.isLooping = !mediaPlayer.isLooping
            loopButton.text = if (mediaPlayer.isLooping) "ON" else "OFF"
        }

        if (audioList.isNotEmpty()) {
            playSong(currentSongIndex)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}