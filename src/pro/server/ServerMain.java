package pro.server;


import pro.Encription.CertificateController;
import pro.Encription.RSA;
import sun.security.x509.X509CertImpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class ServerMain {

    public static void main(String[] args) {
        //CertificateController.setJKSFile("keyStore");
        Socket CASocket;

        String publicKeyPath="PublicKey",
                privateKeyPath="PrivateKey";
        try{
            System.out.println("server started..");
            try {
                X509CertImpl cert =null;
                System.out.println("create cer ? (1 or 0)");
                Scanner scanner = new Scanner(System.in);
                int i = scanner.nextInt();
                if (i == 1){
                    InetAddress adr = InetAddress.getByName(null);
                    CASocket = new Socket(adr, 5999);
                    RSA.generateKeys(publicKeyPath,privateKeyPath);
                    cert =  CertificateController.CAServerClient(CASocket,"cn=server1, O=chat", publicKeyPath, privateKeyPath);
                    CertificateController.saveCert(cert, "");
                }

                if (cert == null){
                    cert = (X509CertImpl) CertificateController.getCert("server1", "");
                }
                //CertificateController.verifyCert(cert);
                ServerSocket serverSocket = new ServerSocket(4999);
                while(true){
                    Socket socket = serverSocket.accept();
                    System.out.println("Client connected");
                    Server server = new Server (socket,cert);
                    server.start();
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        }catch (IOException ignored){
            ignored.printStackTrace();
        }
    }



}
