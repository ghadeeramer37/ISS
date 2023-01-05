package pro.Encription;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {//To get the message hash

    public static String hash(String message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");//using secured hash algorithm to hash the message 
        byte[] hashInBytes = md.digest(message.getBytes(StandardCharsets.UTF_8));
        //transforming bytes to hex
        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
