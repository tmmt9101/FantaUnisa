package it.unisa.fantaunisa.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Security {

    //genera l'hash di una stringa (SHA-512)
    public static String toHash(String password) {
        String hashString = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                sb.append(Integer.toHexString((hash[i] & 0xFF) | 0x100).substring(1, 3));
            }
            hashString = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e);
        }
        return hashString;
    }
}
