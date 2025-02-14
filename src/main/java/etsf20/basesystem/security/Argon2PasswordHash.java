package etsf20.basesystem.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

/**
 * Argon2 - Modern secure password hashing
 * <p>Read more about algorithm: <a href="https://ieeexplore.ieee.org/document/7467361">Argon2 Paper</a>
 */
public class Argon2PasswordHash {

    /**
     * Construct Argon2 parameters
     * @param salt salt to use, make sure it has high entropy (sufficient randomness)
     * @return parameters to use
     */
    private static Argon2Parameters getParameters(byte[] salt) {
         // Configure Argon2 parameters
         Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id);

        // Recommended specifications by OWASP
        // Source: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
        builder.withIterations(2)
               .withMemoryAsKB(19456)
               .withParallelism(1)
               .withSalt(salt);

        return builder.build();
    }

    /**
     * Create new hash of password
     * <b>Remarks</b>: This will generate a new hash each time dependent on the salt
     * @param password plain-text password
     * @see Argon2PasswordHash#verify for verifying a created hash
     * @see Argon2PasswordHash#getParameters for the parameters chosen for this hash
     * @return hashed password, consists of salt + hash encoded as base64
     */
    public static String create(String password) {
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] salt = new byte[16]; // 16 bytes salt
        byte[] hash = new byte[32]; // 32 bytes hash

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        
        generator.init(getParameters(salt));
        generator.generateBytes(passwordBytes, hash);
        
        // Salt must be stored with the password
        Encoder b64encoder = Base64.getEncoder();

        // Encoded as type id (future-proofing for multiple algorithms) $ salt $ hash
        return "1$" + b64encoder.encodeToString(salt) + "$" + b64encoder.encodeToString(hash);
    }

    /**
     * Compare plain-text password with saved hashed variant
     * @param savedHash     salt + hash encoded as base64
     * @param testPassword  plain-text password to check if it matches
     * @return true if saved hash matches test password
     */
    public static boolean verify(String savedHash, String testPassword) {
        String[] parts = savedHash.split("\\$", 3);
        if(parts.length < 3) {
            // incorrect format
            return false;
        }

        Decoder decoder = Base64.getDecoder();

        String type = parts[0];
        byte[] salt = decoder.decode(parts[1]);
        byte[] testHash = decoder.decode(parts[2]);

        if(!type.equals("1")) {
            // type does not match
            return false;
        }

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        
        byte[] passwordBytes = testPassword.getBytes(StandardCharsets.UTF_8);
        byte[] verifyHash = new byte[32]; // 32 bytes hash

        generator.init(getParameters(salt));
        generator.generateBytes(passwordBytes, verifyHash);

        // To mitigate timing attacks, i.e. it does not escape at first non equal byte
        return MessageDigest.isEqual(testHash, verifyHash);
    }

    /**
     * Utility program to generate hashes to include in hardcoded initialization scripts or testing
     */
    public static void main(String[] args) {
        System.out.println("Argon2id Password Generator Utility");
        System.out.println("Enter password:");
        
        try (Scanner scanner = new Scanner(System.in)) {
            if(scanner.hasNextLine()) {
                String password = scanner.nextLine();
                System.out.println(create(password));
            }
        }
    }
}
