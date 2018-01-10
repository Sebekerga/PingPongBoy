package com.sebekerga.linebot;

import android.bluetooth.BluetoothSocket;

/**
 * Created by Maxim on 27.08.2017.
 */

public class SocketHandler {
    private static BluetoothSocket socket;

    public static void setSocket(BluetoothSocket socket){
        SocketHandler.socket = socket;
    }

    public static BluetoothSocket getSocket(){
        return socket;
    }
}
