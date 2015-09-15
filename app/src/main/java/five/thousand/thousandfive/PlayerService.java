package five.thousand.thousandfive;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

public class PlayerService extends Service {

    private static LibVLC mLibVLC;
    private static String mrl;
    public enum State { UNPREPARED, PAUSED, PLAYING }
    public static State state = State.UNPREPARED;

    public PlayerService() {
        mLibVLC = new LibVLC();
        mLibVLC.setHttpReconnect(true);
        mLibVLC.setNetworkCaching(10); // ms
        //TODO: make this configurable over the network.

        mrl = null;
    }

    public static boolean isPlaying() {
        return (mLibVLC != null) && mLibVLC.isPlaying();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            mLibVLC.init(this);
            state = State.UNPREPARED;
        } catch (LibVlcException e) {
            Toast.makeText(this, "Failed to initialize LibVLC", Toast.LENGTH_LONG).show();
            mLibVLC = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;

        if (intent.hasExtra("mrl")) {
            mrl = intent.getStringExtra("mrl");
        }

        if (state == State.UNPREPARED) {
            mLibVLC.playMRL(mrl);
            mLibVLC.pause();
            state = State.PAUSED;
        }


        if (state == State.PAUSED && intent.getBooleanExtra("play", false)) {
            mLibVLC.play();
            //TODO: Thread this away
            state = State.PLAYING;
        }
        else if (state == State.PLAYING && !(intent.getBooleanExtra("play", true))) {
            mLibVLC.stop();
            state = State.PAUSED;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Please don't bind to PlayerService");
    }
}