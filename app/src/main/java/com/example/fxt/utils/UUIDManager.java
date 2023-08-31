package com.example.fxt.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class UUIDManager {

    static public String getDeviceUUID(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("uuid",Context.MODE_PRIVATE);
        final String id = sharedPreferences.getString("uuid",null);

        UUID uuid;
        if (id != null) {
            uuid = UUID.fromString(id);
        } else {
            final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            try {
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                } else {
                    final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("uuid",uuid.toString());
            editor.apply();
        }

        return uuid.toString();
    }
}
