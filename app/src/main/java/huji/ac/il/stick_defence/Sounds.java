package huji.ac.il.stick_defence;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class represents and manages the game sounds.
 */
public class Sounds {
    public static final int WIN_THEME = R.raw.winning_theme;
    public static final int MAIN_THEME = R.raw.main_theme;
    public static final int RUN_SOUND = R.raw.running_sound;
    public static final int WALKING_SOUND = R.raw.walking_sound;
    public static final int ZOMBIE_SOUND = R.raw.zombie_sound;
    public static final int TANK_SOUND = R.raw.tank_sound;
    public static final int OLD_MAN_SOUND = R.raw.old_man_sound;
    public static final int FOG_SOUND = R.raw.wind_sound;
    public static final int POTION_SOUND = R.raw.potion_sound;
    public static final int MATH_BOMB = R.raw.shame;
    public static final int BOW_STRECH = R.raw.bow_strech;
    public static final int BOW_RELEASE = R.raw.bow_release;
    public static final int START_TRUMPET = R.raw.start_trumpet;
    public static final int END_TRUMPET = R.raw.end_trumpet;
    public static final int SMALL_EXPLOSION = R.raw.small_explosion;
    public static final int BIG_EXPLOSTION = R.raw.big_explosion;
    public static final int SWORD_HIT = R.raw.sward_hit;
    public static final int BASIC_HIT = R.raw.basic_hit;
    public static final int BUTTON_CLICK = R.raw.button_click;

    private static MediaPlayer mainThemePlayer;
    private static MediaPlayer winThemePlayer;
    private static Sounds sounds = null;
    private static SoundPool soundPool;
    private static HashMap<Integer, Integer> soundPoolMap;
    private ArrayList<MediaPlayer> registerdMp = new ArrayList<>();
    private Context context;


    private Sounds(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME).setContentType
                            (AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            soundPool = new SoundPool.Builder().setAudioAttributes
                    (attributes).setMaxStreams(3).build();
        } else {
            soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        }
        soundPoolMap = new HashMap<>();
        soundPoolMap.put(RUN_SOUND, soundPool.load(context, RUN_SOUND, 1));
        soundPoolMap
                .put(WALKING_SOUND, soundPool.load(context, WALKING_SOUND, 1));
        soundPoolMap.put(TANK_SOUND, soundPool.load(context, TANK_SOUND, 1));
        soundPoolMap
                .put(ZOMBIE_SOUND, soundPool.load(context, ZOMBIE_SOUND, 1));
        soundPoolMap
                .put(OLD_MAN_SOUND, soundPool.load(context, OLD_MAN_SOUND, 1));
        soundPoolMap.put(BOW_STRECH, soundPool.load(context, BOW_STRECH, 1));
        soundPoolMap.put(BOW_RELEASE, soundPool.load(context, BOW_RELEASE, 1));
        soundPoolMap
                .put(SMALL_EXPLOSION, soundPool.load(context, SMALL_EXPLOSION, 1));
        soundPoolMap
                .put(BIG_EXPLOSTION, soundPool.load(context, BIG_EXPLOSTION, 1));
        soundPoolMap.put(POTION_SOUND, soundPool.load(context, POTION_SOUND, 1));
        soundPoolMap.put(SWORD_HIT, soundPool.load(context, SWORD_HIT, 1));
        soundPoolMap.put(BASIC_HIT, soundPool.load(context, BASIC_HIT, 1));
        soundPoolMap.put(BUTTON_CLICK, soundPool.load(context, BUTTON_CLICK, 1));

    }

    public static Sounds create(Context context) {
        if (sounds == null) {
            sounds = new Sounds(context);
        }
        return sounds;
    }

    public static Sounds getInstance() {
        return sounds;
    }

    public static int playSound(int soundId, boolean loop) {
        float volume = 1;
        int repeat = - 1;
        if (! loop) {
            repeat = 0;
        }
        return soundPool.play(soundPoolMap.get(soundId), volume, volume, 1,
                repeat, 1f);
    }

    public static int playSound(int soundId) {
        float volume = 1;
        return soundPool.play(soundPoolMap.get(soundId), volume, volume, 1, -
                1, 1f);
    }

    public static void stopSound(int streamId) {
        soundPool.pause(streamId);
    }

    public void registerMp(MediaPlayer mp) {
        this.registerdMp.add(mp);
    }

    public void playTheme(int soundID) {

        MediaPlayer mp = MediaPlayer.create(context, soundID);
        mp.setLooping(true);
        if (soundID == MAIN_THEME && mainThemePlayer == null) {

            if (winThemePlayer != null && winThemePlayer.isPlaying()) {
                winThemePlayer.stop();
            }
            mp.start();
            mainThemePlayer = mp;
        }
        if (soundID == WIN_THEME) {
            if (mainThemePlayer != null && mainThemePlayer.isPlaying()) {
                mainThemePlayer.stop();
            }
            mp.start();
            winThemePlayer = mp;
        }
    }

    public MediaPlayer streamSound(int soundId) {
        MediaPlayer mp = MediaPlayer.create(context, soundId);
        mp.setLooping(true);
        mp.start();
        return mp;
    }

    public MediaPlayer streamSound(int soundId, boolean setLooping) {
        MediaPlayer mp = MediaPlayer.create(context, soundId);
        mp.setLooping(setLooping);
        mp.start();
        return mp;
    }

    public void stopAllSound() {
        soundPool.autoPause();
        for (MediaPlayer mp : this.registerdMp) {
            mp.stop();
        }
        this.registerdMp.clear();
    }

    public void stopTheme() {
        if (winThemePlayer != null && winThemePlayer.isPlaying()) {
            winThemePlayer.stop();
            winThemePlayer = null;
        }

        if (mainThemePlayer != null && mainThemePlayer.isPlaying()) {
            mainThemePlayer.stop();
            mainThemePlayer = null;
        }
    }
}
