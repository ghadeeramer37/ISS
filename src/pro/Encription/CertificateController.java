package pro.Encription;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import sun.security.pkcs10.PKCS10;
import sun.security.x509.RDN;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


public class CertificateController {

    static String JKSFile = "keyStore";


    public static PKCS10 genearateCsr(String sub, PublicKey pu, PrivateKey pr) {
        String alg = "SHA256withRSA";
        try {
            PKCS10 pkcs10 = new PKCS10(pu);
            Signature signature = Signature.getInstance(alg);
            signature.initSign(pr);

            X500Name x500Name = new X500Name(sub);
            pkcs10.encodeAndSign(x500Name, signature);

            return pkcs10;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveRootCert(X509CertImpl x509Cert){
        try {

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] pwdArray = "chat".toCharArray();
            ks.load(null, pwdArray);

            try (FileOutputStream fos = new FileOutputStream(JKSFile)) {
                ks.store(fos, pwdArray);
            }

            ks.setCertificateEntry("name", x509Cert);
            ks.store(new FileOutputStream(JKSFile+".jks"), pwdArray);



        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean checkCAKeystore(){
        File file = new File(JKSFile+".jks");
        return !file.exists();
    }


    public static void createKeyStore(String path){
        try {

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] pwdArray = "chat".toCharArray();
            ks.load(null, pwdArray);

            try (FileOutputStream fos = new FileOutputStream(JKSFile+path+".jks")) {
                ks.store(fos, pwdArray);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean checkKeystore(String path){
        File file = new File(JKSFile+path+".jks");
        return !file.exists();
    }

    public static Boolean verifyCert(X509CertImpl cert){
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(JKSFile+".jks"), "chat".toCharArray());
            Certificate caCert = ks.getCertificate("name");
            cert.verify(caCert.getPublicKey());
            System.out.println("good its verified");
            return true;
        }catch (Exception e){
            System.out.println("bad it's not verified");
            return false;
        }
    }

    public static Certificate getCert(String alias, String path){

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(JKSFile+path+".jks"), "chat".toCharArray());
            return ks.getCertificate(alias);
        }catch (Exception e){
            System.out.println("bad it's not found");
            return null;
        }

    }
    public static void saveCert(X509CertImpl cert, String path){
        try{
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(JKSFile + path+".jks"), "chat".toCharArray());
            String alias = X500Name.asX500Name(cert.getSubjectX500Principal()).getCommonName();
            ks.setCertificateEntry(alias, cert);
            ks.store(new FileOutputStream(JKSFile + path+".jks"),"chat".toCharArray());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void saveKey(String alias,String certAlias, PrivateKey privateKey, String path){
        try{
            char[] password = "chat".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(JKSFile + path+".jks"), password);
            Certificate[] certChain = new Certificate[1];
            certChain[0] = getCert(certAlias, path);
            ks.setKeyEntry(alias, privateKey, password,certChain);
            ks.store(new FileOutputStream(JKSFile + path+".jks"),password);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static KeyPair getCertKeys(String alias, String certAlias, String path){
        try {
            char[] password = "chat".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(JKSFile+".jks"), password);
            PrivateKey pr = (PrivateKey) ks.getKey(alias, password);
            PublicKey pu = getCert(certAlias, path).getPublicKey();

            return new KeyPair(pu,pr);

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;

    }


    public static X509CertImpl CAServerClient(Socket socket,String sub, String puPath, String prPath){
        DataOutputStream outToServer;
        DataInputStream inFromServer;
        try {
            inFromServer = new DataInputStream(socket.getInputStream());
            outToServer = new DataOutputStream(socket.getOutputStream());

            PublicKey publicKey=(PublicKey) RSA.ReadObjectFromFile(puPath);
            PrivateKey privateKey =(PrivateKey) RSA.ReadObjectFromFile(prPath);
            PKCS10 csr =  CertificateController.genearateCsr(sub, publicKey, privateKey);
            assert csr != null;
            outToServer.write(csr.getEncoded());

            // Receiving data from server
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            baos.write(buffer, 0 , inFromServer.read(buffer));
            byte[] result = baos.toByteArray();

            X509CertImpl cert = new X509CertImpl(result);
            socket.close();

            return cert;

        } catch (IOException | CertificateException e) {
            System.out.println(e.getMessage());
            try {
                socket.close();
            } catch (IOException e2) {
                System.out.println(e2.getMessage());
            }
        }
        return null;

    }




}