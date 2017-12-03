package com.sebekerga.linebot;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Maxim on 27.08.2017.
 */

public class BluetoothDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    public BluetoothDeviceListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public BluetoothDeviceListAdapter(Context context, int resource, List<BluetoothDevice> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.bluetooth_device_list_item, null);
        }

        BluetoothDevice bluetoothDevice = getItem(position);

        if(bluetoothDevice != null) {
            TextView bluetoothDeviceNameTextView = (TextView) v.findViewById(R.id.bluetooth_device_name);
            TextView bluetoothDeviceMACAddressTextView = (TextView) v.findViewById(R.id.bluetooth_device_mac_address);

            bluetoothDeviceNameTextView.setText(bluetoothDevice.getName().replace("\n", "").replace("\r", ""));
            bluetoothDeviceMACAddressTextView.setText(bluetoothDevice.getAddress());
        }

        return v;
    }
}
