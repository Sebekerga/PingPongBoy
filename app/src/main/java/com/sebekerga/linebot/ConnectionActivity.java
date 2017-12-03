package com.sebekerga.linebot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

public class ConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final List<BluetoothDevice> boundedDevicesList = new LinkedList<>();
        boundedDevicesList.addAll(bluetoothAdapter.getBondedDevices());

        ListView bluetoothDevicesListView = (ListView) findViewById(R.id.devices_list);
        BluetoothDeviceListAdapter bluetoothDeviceListAdapter = new BluetoothDeviceListAdapter(this, R.layout.bluetooth_device_list_item, boundedDevicesList);
        bluetoothDevicesListView.setAdapter(bluetoothDeviceListAdapter);

        bluetoothDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), boundedDevicesList.get(i).getName(), Toast.LENGTH_SHORT).show();

//                BluetoothSocket bluetoothSocket = null;
//                try {
//                    bluetoothSocket = boundedDevicesList.get(i).createInsecureRfcommSocketToServiceRecord(boundedDevicesList.get(i).getUuids()[0].getUuid());
//                    bluetoothSocket.connect();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Toast.makeText(getApplicationContext(), "Error connecting to device", Toast.LENGTH_SHORT).show();
//                }
//
//                SocketHandler.setSocket(bluetoothSocket);
                startActivity(new Intent(ConnectionActivity.this, MainActivity.class));
            }
        });
    }
}
