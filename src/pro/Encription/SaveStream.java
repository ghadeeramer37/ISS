package pro.Encription;

import javafx.util.Pair;
import pro.server.DataBase;
import pro.server.RequestApi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

public class SaveStream {

    public static Pair<String, Boolean> receiveString(DataInputStream dataInputStream, String key) throws IOException {
        AES aes = new AES();
        String iv = dataInputStream.readUTF();

        aes.setKey(key);
        aes.setInitVector(iv);
        String encryptMessage = dataInputStream.readUTF();
        String hash = dataInputStream.readUTF();
        boolean b = IsMessageCorrect(encryptMessage, hash, key);
        return new Pair<String, Boolean>(aes.decrypt(encryptMessage), b);
    }

    public static Pair<String, Boolean> receiveString(DataInputStream dataInputStream, String key, int user_id) throws IOException {
        AES aes = new AES();
        String iv = dataInputStream.readUTF();

        aes.setKey(key);
        aes.setInitVector(iv);
        String encryptMessage = dataInputStream.readUTF();
        boolean b = false;
        boolean verifySignature = false;
        try {
            DataBase dataBase = new DataBase();
            dataBase.openConnection();
            String hash = dataInputStream.readUTF();
            PublicKey clientPublicKey = dataBase.getClientPublicKey(user_id);
            verifySignature = DigitalSignature.VerifyDigitalSignature(encryptMessage + key, hash, clientPublicKey);
            dataBase.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pair<String, Boolean>(aes.decrypt(encryptMessage),/*b*/ verifySignature);
    }


    public static Pair<String, Boolean> receiveString(DataInputStream dataInputStream, String key, PublicKey publicKey) throws IOException {
        AES aes = new AES();
        String iv = dataInputStream.readUTF();

        aes.setKey(key);
        aes.setInitVector(iv);
        String encryptMessage = dataInputStream.readUTF();
        boolean b = false;
        boolean verifySignature = false;
        try {
            String hash = dataInputStream.readUTF();
            verifySignature = DigitalSignature.VerifyDigitalSignature(encryptMessage + key, hash, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pair<String, Boolean>(aes.decrypt(encryptMessage),/*b*/ verifySignature);
    }


    public static void sendString(DataOutputStream dataOutputStream, String key, String message) throws IOException {
        AES aes = new AES();
        aes.setRandomInitVector();
        dataOutputStream.writeUTF(aes.getInitVector());

        aes.setKey(key);
        String encryptMessage = aes.encrypt(message);

        dataOutputStream.writeUTF(encryptMessage);
        try {
            dataOutputStream.writeUTF(Hashing.hash(encryptMessage + key));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }


    public static void sendString(DataOutputStream dataOutputStream, String key, String message, PrivateKey pr) throws IOException {
        AES aes = new AES();
        aes.setRandomInitVector();
        dataOutputStream.writeUTF(aes.getInitVector());

        aes.setKey(key);
        String encryptMessage = aes.encrypt(message);

        dataOutputStream.writeUTF(encryptMessage);

        try {
            dataOutputStream.writeUTF(DigitalSignature.CreateDigitalSignature(encryptMessage + key, pr));
        } catch (Exception e) {

            e.printStackTrace();
        }

    }


    public static boolean IsMessageCorrect(String message, String hash, String key) {
        try {
            return hash.equals(Hashing.hash(message + key));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Pair<String, Boolean> receiveApiString(DataInputStream dataInputStream, String key, int user_id) throws IOException {
        AES aes = new AES();
        String iv = dataInputStream.readUTF();
        aes.setKey(key);
        aes.setInitVector(iv);
        String encryptMessage = dataInputStream.readUTF();
        RequestApi.setEncryptRequestApi(encryptMessage);
        RequestApi.setDate(new Date());

        boolean b = false;
        boolean verifySignature = false;
        try {
            DataBase sqlConnection = new DataBase();
            sqlConnection.openConnection();
            String hash = dataInputStream.readUTF();
            RequestApi.setDS(hash);
            RequestApi.setApiKey(encryptMessage + key);

            verifySignature = DigitalSignature.VerifyDigitalSignature(
                    /*Hashing.hash(encryptMessage + key)*/encryptMessage + key,
                    hash,
                    sqlConnection.getClientPublicKey(user_id));

            sqlConnection.closeConnection();
/*            if (verifySignature){
                b=IsMessageCorrect(encryptMessage,hash,key);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pair<>(aes.decrypt(encryptMessage),/*b*/ verifySignature);
    }
}
