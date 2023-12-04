package com.fiberfox.fxt.utils;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncrytData {

    private static String CIPHER_NAME = "AES/CBC/PKCS5PADDING";

    private static int CIPHER_KEY_LEN = 32; //128 bits

    private static String KEY   = "fiberfox_ijoon_20221219secretkey"; // key to use should be 16 bytes long (128 bits)
    private static String IV    = "0123456789012345"; // initialization vector

    /**
     * Encrypt data using AES Cipher (CBC) with 128 bit key
     *
     * @param data - data to encrypt
     * @return encryptedData data in base64 encoding with iv attached at end after a :
     */
    public static String encrypt(String data) {

        try {
            byte[] textBytes = data.getBytes("UTF-8");
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
            SecretKeySpec newKey = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
            Cipher cipher = null;
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

            return Base64.getMimeEncoder().encodeToString(cipher.doFinal(textBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private static String fixKey(String key) {

        if (key.length() < EncrytData.CIPHER_KEY_LEN) {
            int numPad = EncrytData.CIPHER_KEY_LEN - key.length();

            for (int i = 0; i < numPad; i++) {
                key += "0"; //0 pad to len 16 bytes
            }

            return key;

        }

        if (key.length() > EncrytData.CIPHER_KEY_LEN) {
            return key.substring(0, CIPHER_KEY_LEN); //truncate to 16 bytes
        }

        return key;
    }

    /**
     * Decrypt data using AES Cipher (CBC) with 128 bit key
     *
     * @param data - encrypted data with iv at the end separate by :
     * @return decrypted data string
     */

    public static String decrypt(String data) {

        try {
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes("UTF-8"));
            // SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
            SecretKeySpec secretKey = new SecretKeySpec(fixKey(KEY).getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance(EncrytData.CIPHER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decodedEncryptedData = android.util.Base64.decode(data, android.util.Base64.DEFAULT); //Base64.getDecoder().decode(parts[0]);

            byte[] original = cipher.doFinal(decodedEncryptedData);

            return new String(original);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}