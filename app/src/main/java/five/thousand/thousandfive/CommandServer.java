package five.thousand.thousandfive;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

import five.thousand.thousandfive.Commands.Mute;
import five.thousand.thousandfive.Commands.Stop;
import five.thousand.thousandfive.Commands.Play;
import five.thousand.thousandfive.utils.CommandSerialization;

public class CommandServer extends Service {
    private Server server;

    final Listener commandListener = new Listener() {
        @Override
        public void received(Connection conn, Object obj) {
            super.received(conn, obj);
            if (obj instanceof Mute)
                PlayerService.mute((Mute)obj);
            else if (obj instanceof Play)
                PlayerService.play(((Play)obj).MRL);
            else if (obj instanceof Stop && PlayerService.isPlaying())
                PlayerService.play(null);
            conn.close();
        }
    };

    public CommandServer() {
        CommandSerialization cs = new CommandSerialization();
        cs.register(Play.class);
        cs.register(Stop.class);
        cs.register(Mute.class);
        server = new Server(16384, 2048, cs);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                super.connected(connection);
                connection.addListener(commandListener);
            }
        });

        try{
            server.bind(4443);
        }
        catch (IOException e)
        {
            Toast.makeText(this, "Couldn't listen for commands", Toast.LENGTH_LONG).show();
        }
        server.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
