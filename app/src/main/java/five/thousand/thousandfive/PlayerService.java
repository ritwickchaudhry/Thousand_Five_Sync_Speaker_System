package five.thousand.thousandfive;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

import java.util.Timer;
import java.util.TimerTask;

import five.thousand.thousandfive.Commands.Mute;

public class PlayerService extends Service {

    private static LibVLC mLibVLC;
    private static String mrl;
    private static Context mContext;
    private static long pre;

    public enum State { UNPREPARED, STOPPED, PLAYING }
    public static State state = State.UNPREPARED;

    public PlayerService() throws LibVlcException {
        mLibVLC = LibVLC.getInstance();
        mLibVLC.setHttpReconnect(true);
        mLibVLC.setNetworkCaching(10); // ms
        //TODO: make this configurable over the network.

        mrl = null;

        mContext = this;
    }

    public static boolean isPlaying() {
//        return (mLibVLC != null) && mLibVLC.isPlaying();
        if (mLibVLC != null)
            return mLibVLC.isPlaying();
        else
            return false;
    }

    public static void play(String mrl) {
        if (mLibVLC == null) {
            Log.e("PlayerService", "PlayerService.play called without initialization");
            return;
        }
        if(mrl != null)
            if (mrl.equals("")) mLibVLC.play();
            else mLibVLC.playMRL(mrl);
        else mLibVLC.stop();
        state = (mrl != null) ? State.PLAYING : State.STOPPED;
    }

    public static void mute(Mute mute) {
        if (mLibVLC == null) {
            Log.e("PlayerService", "PlayerService.mute called without initialization");
            return;
        }
        mute.doMute(mLibVLC);
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
        super.onStartCommand(intent, flags, startId);

        if (intent.hasExtra("mrl")) {
            mrl = intent.getStringExtra("mrl");
        }

        if (state == State.UNPREPARED) {
            mLibVLC.playMRL(mrl);
            mLibVLC.stop();
            state = State.STOPPED;
        }


        if (state == State.STOPPED && intent.getBooleanExtra("play", false)) {
            mLibVLC.play();
            //TODO: Thread this away
            state = State.PLAYING;
        }
        else if (state == State.PLAYING && !(intent.getBooleanExtra("play", true))) {
            mLibVLC.stop();
            state = State.STOPPED;
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Please don't bind to PlayerService");
    }
}