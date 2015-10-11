package five.thousand.thousandfive;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.esotericsoftware.jsonbeans.JsonException;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import five.thousand.thousandfive.Commands.Mrl;
import five.thousand.thousandfive.Commands.Mute;
import five.thousand.thousandfive.Commands.Play;
import five.thousand.thousandfive.Commands.Stop;
import five.thousand.thousandfive.utils.CommandSerialization;

public class CommandServer extends Service {
    private Server server;
    private static CommandSerialization cs;
    private final Integer brdPort = 4444;
    private Thread broadcastReceiveThread;

    final Listener commandListener = new Listener() {
        @Override
        public void received(Connection conn, Object obj) {
            if (conn != null) super.received(conn, obj); //HACK: conn != null
            if (obj instanceof Mute)
                PlayerService.mute((Mute)obj);
            else if (obj instanceof Play)
                PlayerService.play(((Play)obj).MRL);
            else if (obj instanceof Stop && PlayerService.isPlaying())
                PlayerService.play(null);
            if (conn != null) conn.close(); //HACK: conn != null
        }
    };

    public CommandServer() {
        cs = new CommandSerialization();
        cs.register(Play.class);
        cs.register(Stop.class);
        cs.register(Mute.class);
        cs.register(Mrl.class);
        server = new Server(16384, 2048, cs);
    }

    public static Object readCommand(String str) {
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());
        return cs.read(null, buffer);
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

        final InetAddress brd = getBroadcastAddress();
        if (brd != null) {
            broadcastReceiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] buf = new byte[4096];
                        ByteBuffer commandBuf;
                        DatagramSocket socket = new DatagramSocket(brdPort, brd);
                        DatagramPacket packet;
                        socket.setBroadcast(true);
                        Object obj;
                        while(!broadcastReceiveThread.isInterrupted()) {
                            buf[0] = buf[1] = buf[2] = buf[3] = 0; // Reset command length to zero
                            packet = new DatagramPacket(buf, buf.length);
                            try {
                                socket.receive(packet);
                            } catch (IOException e) {
                                continue;
                            }
                            commandBuf = ByteBuffer.wrap(packet.getData());
                            int length = cs.readLength(commandBuf);
                            if (commandBuf.capacity() < length) continue;
                            commandBuf.compact(); commandBuf.flip(); commandBuf.limit(length);
                            try {
                                obj = cs.read(null, commandBuf);
                            } catch (JsonException e) {
                                continue;
                            }
                            commandListener.received(null, obj);
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }
            });
            broadcastReceiveThread.start();
        }

        try{
            server.bind(4443, 4443);
        }
        catch (IOException e)
        {
            Log.d("CommandServer", "Couldn't listen for commands");
        }
        server.start();
    }

    InetAddress getBroadcastAddress() {
        WifiManager wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null) return null;

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        try {
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            return null; //Shouldn't happen
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastReceiveThread.interrupt();
        broadcastReceiveThread = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
