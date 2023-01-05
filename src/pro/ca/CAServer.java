package pro.ca;

import pro.Encription.CertificateController;
import sun.security.pkcs10.PKCS10;
import sun.security.x509.*;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Date;

public class CAServer {
    public static void main(String[] args){
        try {
            ServerSocket serverSocket = new ServerSocket(5999);
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");
            CAServerThread caServerThread = new CAServerThread(socket);
            caServerThread.start();
        }catch (Exception e){
            e.printStackTrace();
        }



    }
}

class CAServerThread extends Thread{
    private final DataOutputStream outToClient;
    private final DataInputStream inFromClient;
    private static final String BC_PROVIDER = "BC";
    private static final String KEY_ALGORITHM = "RSA";
    String dn = "name";
    String KeyGeneratorALg = "RSA";
    private X509CertImpl cert;
    private KeyPair pair;

    CAServerThread(Socket socket) throws IOException {
        outToClient = new DataOutputStream(socket.getOutputStream());
        inFromClient = new DataInputStream(socket.getInputStream());
        createRootCertificate();
    }


    public void run(){
        try {
            PKCS10 csr = recieveCSR();
            assert csr != null;
            byte[] cerByte = createCertificate(csr).getEncoded();
            outToClient.write(cerByte);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void createRootCertificate(){
        if (!CertificateController.checkCAKeystore()){
            KeyPair tempPair = CertificateController.getCertKeys("namePr", "name", "");
            assert tempPair != null;
            pair = new KeyPair(tempPair.getPublic(),tempPair.getPrivate());
            return;
        }
        try {
            int days = 365;
            generateKeys(KeyGeneratorALg);
            PrivateKey privkey = pair.getPrivate();

            X509CertInfo info = new X509CertInfo();

            Date from = new Date();
            Date to = new Date(from.getTime() + days * 86400000L);
            CertificateValidity interval = new CertificateValidity(from, to);

            BigInteger sn = new BigInteger(64, new SecureRandom());
            X500Name owner = new X500Name("cn="+dn);

            info.set(X509CertInfo.VALIDITY, interval);
            info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
            info.set(X509CertInfo.SUBJECT, owner);
            info.set(X509CertInfo.ISSUER, owner);
            info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
            info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
            info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

            // Sign the cert to identify the algorithm that's used.
            cert = new X509CertImpl(info);
            cert.sign(privkey, "SHA1withRSA");

            // Update the algorith, and resign.
            algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
            info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
            cert = new X509CertImpl(info);
            cert.sign(privkey, "SHA1withRSA");

            CertificateController.saveRootCert(cert);
            CertificateController.saveKey("namePr","name",privkey,"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateKeys(String algorithm) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        keyPairGenerator.initialize(1024, secureRandom);
        pair = keyPairGenerator.generateKeyPair();

        System.out.println("Key Pair Generated....");
    }

    private PKCS10 recieveCSR() throws IOException, SignatureException, NoSuchAlgorithmException {
        // Receiving data from Client
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        baos.write(buffer, 0 , inFromClient.read(buffer));
        byte[] result = baos.toByteArray();

        PKCS10 csr = new PKCS10(result);
        String cn = csr.getSubjectName().getCommonName();
        System.out.println(cn);

        if (validateCsr(cn)){
            return csr;
        }
        return null;
    }



    private boolean validateCsr(String cn) throws IOException {
        return true;
    }

    private X509CertImpl createCertificate(PKCS10 csr) throws IOException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        int days = 365;
        X509CertInfo info = new X509CertInfo();

        Date from = new Date();
        Date to = new Date(from.getTime() + days * 86400000L);
        CertificateValidity interval = new CertificateValidity(from, to);

        BigInteger sn = new BigInteger(64, new SecureRandom());

/*
        X500Name owner = new X500Name("cn="+csr.getSubjectName().getCommonName()+", O=passwordManager" );
*/

        X500Name owner = csr.getSubjectName();
        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, owner);
        info.set(X509CertInfo.ISSUER, owner);
        info.set(X509CertInfo.KEY, new CertificateX509Key(csr.getSubjectPublicKeyInfo()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        // Sign the cert to identify the algorithm that's used.
        cert = new X509CertImpl(info);
        cert.sign(pair.getPrivate(), "SHA1withRSA");

        // Update the algorith, and resign.
        algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(pair.getPrivate(), "SHA1withRSA");

        return cert;
    }
}
