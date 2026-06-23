---
name: audio-sfx-integrator
description: "Implements audio playback: sound effects from cache index 4 (jingles/SFX), music tracks from cache index 6 (MIDI/OGG), area-based music transitions, and volume control."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Audio & SFX Integrator

You implement the OSRS audio system for sound effects and music.

## Cache Audio Indices

```
Index 4  = Sound effects (RIFF WAVE encoded, ~4000 SFX)
Index 6  = Music tracks (MIDI encoded, 600+ tracks)
Index 11 = Music jingles (short MIDI clips)
```

## Sound Effect System

```kotlin
object SoundEffectPlayer {
    private val audioClipCache = LRUCache<Int, AudioClip>(256)
    private var sfxVolume = 100  // 0-100

    fun play(soundId: Int, delay: Int = 0, loops: Int = 1) {
        if (sfxVolume == 0) return
        val clip = audioClipCache.getOrLoad(soundId) {
            loadFromCache(soundId)
        }
        clip?.let {
            it.volume = sfxVolume / 100f
            if (delay > 0) schedulePlay(it, delay)
            else it.start()
        }
    }

    private fun loadFromCache(soundId: Int): AudioClip? {
        val data = CacheManager.getSound(soundId) ?: return null
        return AudioSystem.getClip().apply {
            open(AudioSystem.getAudioInputStream(data.inputStream()))
        }
    }
}
```

## Music System

OSRS music uses MIDI sequences with custom soundfont:

```kotlin
object MusicPlayer {
    private var sequencer: Sequencer? = null
    private var currentTrackId = -1
    private var musicVolume = 100

    fun playTrack(trackId: Int, fadeIn: Boolean = true) {
        if (trackId == currentTrackId) return
        currentTrackId = trackId

        val midiData = CacheManager.getMusic(trackId) ?: return
        sequencer?.stop()
        sequencer = MidiSystem.getSequencer().apply {
            open()
            sequence = MidiSystem.getSequence(midiData.inputStream())
            setVolume(musicVolume)
            if (fadeIn) fadeIn(durationMs = 2000)
            start()
        }
    }

    fun stop(fadeOut: Boolean = true) {
        if (fadeOut) fadeOut(durationMs = 1000) { sequencer?.stop() }
        else sequencer?.stop()
    }
}
```

## Area Music Transitions

Music changes based on player map region:

```kotlin
object AreaMusicManager {
    // regionId → musicTrackId (loaded from cache music regions)
    private val regionMusicMap = HashMap<Int, Int>()

    fun onRegionChange(oldRegion: Int, newRegion: Int) {
        val newTrack = regionMusicMap[newRegion] ?: return
        val currentTrack = regionMusicMap[oldRegion] ?: -1
        if (newTrack != currentTrack) {
            MusicPlayer.playTrack(newTrack, fadeIn = true)
        }
    }
}
```

## MIDI Soundfont

OSRS uses a custom soundfont (`soundfont.sf2`) for music playback.
The soundfont is stored in the gamepack JAR, not the cache.
For private server use, substitute with a compatible General MIDI soundfont.

## Volume Configuration

```kotlin
data class AudioConfig(
    var musicVolume: Int = 100,    // 0-100
    var sfxVolume: Int = 100,      // 0-100
    var areaVolume: Int = 100,     // ambient sounds
    var musicEnabled: Boolean = true,
    var sfxEnabled: Boolean = true
)
```

## Common Sound Effect IDs

```
2277 = Level up jingle
3914 = Item pickup
2581 = Attack (generic)
220  = Eating
2748 = Quest complete
```

## Implementation Notes

- SFX are fire-and-forget; not positional (no 3D audio)
- Music loops continuously; transitions use crossfade
- Some SFX have area volume reduction based on distance
- `AudioConfig` persists to player settings file
