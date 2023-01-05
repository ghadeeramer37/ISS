package pro.client;


import pro.Encription.CertificateController;
import pro.Encription.RSA;
import sun.security.x509.X509CertImpl;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.InvalidNameException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Scanner;

public class ClientMain {


    public static void main(String[] args) throws IOException {

        Socket s = new Socket("localhost", 4999);
        Client client = new Client(s);

        client.generateSecretKey();
        try {
            client.receiveCertfifcate();
            // client.receivePublicKey();
            try {
                client.sendSecretKey();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException | CertificateException | InvalidNameException e) {
            e.printStackTrace();
        }

        if (!loginRegisterForm(client)) {
            client.close1();
            return;
        }
        //client.setSecretKey();
        client.continue_();
        try {
            clientForm(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean loginRegisterForm(Client client) {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter:");
        System.out.println("\t 1 : login");
        System.out.println("\t 2 : register");
        System.out.println("\t 3 : exit ");
        System.out.print("Your choice: ");
        int choice = input.nextInt();
        switch (choice) {
            case 1:
                if (client.login()) {
                    System.out.println("login successful");
                    return true;
                } else {
                    System.out.println("login field");
                    return false;
                }
            case 2:
                if (client.register()) {
                    System.out.println("register successful");
                    return true;
                } else {
                    System.out.println("register field..choose another user name");
                    return false;
                }

            default:
                return false;

        }
    }

    public static void clientForm(Client client) throws IOException {
        int choice;
        Scanner scanner = new Scanner(System.in);
        client.receiveNumbersFromServer();
        try {
            client.receiveMessageFromServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            System.out.println("\t1 : to send message.");
            System.out.println("\t2 : to view all messages.");
            System.out.println("\t3 : to add new friend.");
            System.out.println("\t4 : to create Client Certificate.");
            System.out.println("\t5 : close.");
            choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    if (client.send()) {
                        System.out.println("send  successful");
                    } else {
                        System.out.println("send field..try again");
                    }
                    break;
                case 2:
                    client.showMessages();
                    break;
                case 3:
                    if (client.addFriend()) {
                        System.out.println("add friend successful");
                        client.receiveNumbersFromServer();
                    } else {
                        System.out.println("add friend field..try again");
                    }
                    break;
                case 4:
                    client.certificate();
                    System.out.println("create Certificate successful");
                    break;
                default:
                    client.close();
                    return;
            }
        }

    }


}
