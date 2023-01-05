package pro.Encription;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;


public class DigitalSignature {

    // Signing Algorithm
    private static final String
            SIGNING_ALGORITHM
            = "SHA256withRSA";

    // Function to implement Digital signature
    // using SHA256 and RSA algorithm
    // by passing private key.
    public static String bytesToString(byte []  bytes){


        // put data into this char array
        char[] cbuf = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            cbuf[i] = (char) bytes[i];
        }
        // this is the string
        String s = new String(cbuf);
        return s;
    }
    public static byte[] stringToBytes(String s){
        byte[] out = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            out[i] = (byte) s.charAt(i);
        }
        return out;

    }
    public static String CreateDigitalSignature(String input, PrivateKey Key) throws Exception
    {
        Signature signature
                = Signature.getInstance(
                SIGNING_ALGORITHM);
        signature.initSign(Key);
        signature.update(input.getBytes());
        return bytesToString(signature.sign());
    }
    // Function for Verification of the
    // digital signature by using the public key
    public static boolean VerifyDigitalSignature(String input, String signatureToVerify, PublicKey key) throws Exception
    {
        Signature signature
                = Signature.getInstance(
                SIGNING_ALGORITHM);
        signature.initVerify(key);

        signature.update(input.getBytes());

        return signature.verify(stringToBytes(signatureToVerify));
    }


}

