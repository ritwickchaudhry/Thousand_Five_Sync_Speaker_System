package five.thousand.thousandfive;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

public class PlayerService extends Service {

    private LibVLC mLibVLC;
    private State state;
    private String mrl;
    private enum State {
        UNPREPARED,
        PAUSED,
        PLAYING
    }

    public PlayerService() {
        mLibVLC = new LibVLC();
        mLibVLC.setHttpReconnect(true);
        mLibVLC.setNetworkCaching(1000); // ms
        //TODO: make this configurable over the network.

        mrl = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            mLibVLC.init(this);
            state = State.UNPREPARED;
            Toast.makeText(this, "LibVLC ready", Toast.LENGTH_SHORT).show();
        } catch (LibVlcException e) {
            Toast.makeText(this, "Failed to initialize LibVLC", Toast.LENGTH_LONG).show();
            mLibVLC = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mrl == null) {
            mrl = intent.getStringExtra("mrl");
        }

        if (state == State.UNPREPARED)
            mLibVLC.playMRL(mrl);
        else if (state == State.PAUSED && intent.getBooleanExtra("play", false))
            mLibVLC.play();
        //TODO: Thread this away
        else if (state == State.PLAYING && !intent.getBooleanExtra("play", true))
            mLibVLC.stop();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Please don't bind to PlayerService");
    }
}