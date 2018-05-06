package com.kidosc.kidomusic.audio;

/**
 * Created by jason.xu on 2018/5/4.
 */
public interface IAudioPlayer {
    void setDataSource(String dataSource);

    boolean stop();

    boolean playOrPause();

    boolean seek(int pos);

    int getPosition();

    boolean isPlaying();

    void setVolume(int volume);

    int getVolume();

    boolean isAlive();

    void reset();
}
