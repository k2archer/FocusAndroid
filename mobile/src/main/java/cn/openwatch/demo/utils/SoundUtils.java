package cn.openwatch.demo.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

import cn.openwatch.demo.R;

public class SoundUtils {

    SoundPool sp; // 声明SoundPool的引用
    HashMap<Integer, Integer> hm; // 声明一个HashMap来存放声音文件
    int currStreamId;// 当前正播放的streamId

    // 初始化声音池的方法
    public void initSoundPool(Context context) {
        sp = new SoundPool(4, AudioManager.STREAM_MUSIC, 0); // 创建SoundPool对象
        hm = new HashMap<Integer, Integer>(); // 创建HashMap对象
        hm.put(1, sp.load(context, R.raw.ticking, 1)); // 加载声音文件musictest并且设置为1号声音放入hm中
        hm.put(2, sp.load(context, R.raw.alarm, 1)); // 加载声音文件musictest并且设置为1号声音放入hm中
    }

    // 播放声音的方法
    public void playSound(Context context, int sound, int loop) { // 获取AudioManager引用
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // 获取当前音量
        float streamVolumeCurrent = am
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        // 获取系统最大音量
        float streamVolumeMax = am
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 计算得到播放音量
        float volume = streamVolumeCurrent / streamVolumeMax;
        volume = 0.03f;
        // 调用SoundPool的play方法来播放声音文件
        currStreamId = sp.play(hm.get(sound), volume, volume, 1, loop, 1.0f);
    }
}
