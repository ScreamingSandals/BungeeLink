package org.screamingsandals.bungeelink.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Encryption {
    public static byte[] encrypt(String strToEncrypt, String secret) {
        try {
            var iv = new byte[16];
            var ivspec = new IvParameterSpec(iv);

            var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            var spec = new PBEKeySpec(secret.toCharArray(), generateSalt(toSeed(secret)), 65536, 256);
            var tmp = factory.generateSecret(spec);
            var secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encode(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String decrypt(byte[] strToDecrypt, String secret) {
        try {
            var iv = new byte[16];
            var ivspec = new IvParameterSpec(iv);

            var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            var spec = new PBEKeySpec(secret.toCharArray(), generateSalt(toSeed(secret)), 65536, 256);
            var tmp = factory.generateSecret(spec);
            var secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            var cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception ignored) {
        }
        return null;
    }

    public static long toSeed(String string) {
        var seed = new AtomicLong(0);
        string.chars().forEach(seed::addAndGet);
        return seed.get();
    }

    public static byte[] generateSalt(long seed) {
        var random = new Random(seed);
        var saltBytes = new byte[8];
        random.nextBytes(saltBytes);
        return saltBytes;
    }

    public static String randomString(int length) {
        int leftLimit = 48;
        int rightLimit = 122;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
