package pro;

import pro.server.DataBase;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import pro.Encription.*;

public class Main {


    public static void main(String[] args) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
        PublicKey pk=(PublicKey) ReadObjectFromFile("PublicKey");
        PrivateKey pp=(PrivateKey)ReadObjectFromFile("PrivateKey");
        String s,h;
        s="hello world";
        h= RSA.encrypt(s,pk);
        System.out.println(h);
        s=RSA.decrypt(h,pp);
        System.out.println(s);
    }


    public static Object ReadObjectFromFile(String filepath) {

        try {

            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);

            Object obj = objectIn.readObject();


            objectIn.close();
            return obj;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
