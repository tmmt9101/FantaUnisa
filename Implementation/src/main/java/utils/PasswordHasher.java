package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Descrizione: Classe di utilità che fornisce algoritmi crittografici per l'hashing sicuro delle password.
 */
public class PasswordHasher {

    /**
     * Esegue l'hashing della password utilizzando l'algoritmo SHA-256.
     * * @param password La password in chiaro da crittografare.
     * @return Una stringa esadecimale rappresentante l'hash della password,
     * oppure null se l'input non è valido.
     * * Pre-condizioni: La stringa di input non deve essere nulla o vuota.
     * Post-condizioni: Restituisce una stringa esadecimale di lunghezza fissa.
     * Invariante: L'algoritmo è deterministico.
     */
    public static String hash(String password) {

        if (password == null || password.trim().isEmpty()) {
            return null;
        }

        try {
            // Istanza dell'algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Esecuzione dell'hashing sui byte della stringa (UTF-8)
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Conversione da byte array a stringa esadecimale (Hex String)
            return bytesToHex(encodedhash);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore critico: Algoritmo SHA-256 non trovato.");
        }
    }

    /**
     * Verifica se una password in chiaro corrisponde a un hash memorizzato.
     * * @param password La password in chiaro fornita dall'utente.
     * @param hashedPassword L'hash memorizzato nel database.
     * @return true se l'hash della password fornita coincide con quello memorizzato.
     */
    public static boolean verify(String password, String hashedPassword) {
        // Se uno dei due è nullo, la verifica fallisce
        if (password == null || hashedPassword == null) {
            return false;
        }

        // Calcola l'hash della password in ingresso
        String computedHash = hash(password);

        // Confronta l'hash calcolato con quello salvato
        // Utilizziamo equals() perché sono stringhe
        return computedHash != null && computedHash.equals(hashedPassword);
    }

    /**
     * Metodo helper privato per convertire un array di byte in una stringa esadecimale.
     * Necessario perché SHA-256 restituisce byte grezzi, ma il DB si aspetta una Stringa.
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}