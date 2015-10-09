package five.thousand.thousandfive.Commands;

import org.videolan.libvlc.LibVLC;

import java.util.Timer;
import java.util.TimerTask;

public class Mute {
    public int duration = 0;
    public int level = 0;

    public void doMute(final LibVLC mLibVLC) {
        mLibVLC.setVolume(Math.min(level, 100));
        if (duration > 0) {
            Timer unmuter = new Timer(true);
            unmuter.schedule(new TimerTask() {
                @Override
                public void run() {
                    mLibVLC.setVolume(100);
                }
            }, duration);
        }
    }
}
