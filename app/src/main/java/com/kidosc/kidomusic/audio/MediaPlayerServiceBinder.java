package com.kidosc.kidomusic.audio;

import android.os.Binder;

/**
 * Created by jason.xu on 2018/5/4.
 */
public class MediaPlayerServiceBinder extends Binder implements IAudioPlayer{
    private MediaPlayerService service;

    public MediaPlayerServiceBinder (MediaPlayerService service) {
        this.service = service;
    }

    @Override
    public boolean isBinderAlive() {
        return service != null;
    }

    @Override
    public void setDataSource(String dataSource) {
        service.setDataSource(dataSource);
    }

    @Override
    public boolean stop() {
        return service.stop();
    }

    @Override
    public boolean playOrPause() {
        return service.playOrPause();
    }

    @Override
    public boolean seek(int pos) {
        return service.seek(pos);
    }

    @Override
    public int getPosition() {
        return service.getPosition();
    }

    @Override
    public boolean isPlaying() {
        return service.isPlaying();
    }

    @Override
    public void setVolume(int volume) {
        service.setVolume(volume);
    }

    @Override
    public int getVolume() {
        return service.getVolume();
    }

    @Override
    public boolean isAlive() {
        return service.isAlive();
    }

    @Override
    public void reset() {
        service.reset();
    }
}
