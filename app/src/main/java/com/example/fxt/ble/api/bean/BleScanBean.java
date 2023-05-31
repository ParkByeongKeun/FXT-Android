package com.example.fxt.ble.api.bean;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;


public class BleScanBean implements Parcelable {
    private BluetoothDevice mBluetoothDevice;
    private String address;
    private String name;
    private int rssi;

    public BleScanBean(BluetoothDevice bluetoothDevice, int rssi) {
        setBluetoothDevice(bluetoothDevice);
        this.rssi = rssi;
    }

    protected BleScanBean(Parcel in) {
        mBluetoothDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        address = in.readString();
        name = in.readString();
        rssi = in.readInt();
    }

    public static final Creator<BleScanBean> CREATOR = new Creator<BleScanBean>() {
        @Override
        public BleScanBean createFromParcel(Parcel in) {
            return new BleScanBean(in);
        }

        @Override
        public BleScanBean[] newArray(int size) {
            return new BleScanBean[size];
        }
    };

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        mBluetoothDevice = bluetoothDevice;
        if (mBluetoothDevice != null) {
            address = mBluetoothDevice.getAddress();
            name = mBluetoothDevice.getName();
        }
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public int getRssi() {
        return rssi;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mBluetoothDevice, flags);
        dest.writeString(address);
        dest.writeString(name);
        dest.writeInt(rssi);
    }

    @Override
    public String toString() {
        return "BleScanBean{" +
                "mBluetoothDevice=" + mBluetoothDevice +
                ", address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", rssi=" + rssi +
                '}';
    }
}
