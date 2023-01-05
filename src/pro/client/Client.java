package pro.client;


import javafx.util.Pair;
import pro.Encription.CertificateController;
import pro.Encription.RSA;
import pro.Encription.SaveStream;
import sun.security.x509.X509CertImpl;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {


    private Socket socket;
    private String password;
    private int number;
    private DataInputStream inFromServer;
    private DataOutputStream outToServer;
    private Api api;
    private ArrayList<Message> messages;
    private String secretKey;
    private PublicKey serverPublicKey;
    private PrivateKey ClientPrivateKey;
    private PublicKey ClientPublicKey;
    private String ClientpublicKeyPath = "ClinetPublicKeyss",
            ClientprivateKeyPath = "ClientPrivateKeyss";
    private ArrayList<Pair<Integer, String>> numbers = new ArrayList<Pair<Integer, String>>();

    public void setSecretKey() {
        this.secretKey = this.password;
    }

    public Client(Socket soc) {
        this.api = new Api();
        this.messages = new ArrayList<Message>();
        try {
            System.out.println("localhost");
            socket = soc;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            inFromServer = new DataInputStream(socket.getInputStream());
            outToServer = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            try {
                socket.close();
            } catch (IOException e2) {
                System.out.println(e2.getMessage());
            }
        }
    }

    private void setNumber(int number) {

        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public boolean login() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please Enter Your Number:");
        setNumber(input.nextInt());
        System.out.println("Please Enter Your Password:");
        setPassword(input.next());
        try {

            SaveStream.sendString(outToServer, secretKey, this.api.getLoginApi());
            // outToServer.writeUTF(this.api.getLoginApi());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error In Api Login ");
            return false;
        }
        try {
            //outToServer.writeInt(getNumber());
            //outToServer.writeUTF(getPassword());
            SaveStream.sendString(outToServer, secretKey, String.valueOf(getNumber()));
            SaveStream.sendString(outToServer, secretKey, getPassword());
            if (inFromServer.readBoolean()) {
                ClientPublicKey = (PublicKey) RSA.ReadObjectFromFile(ClientpublicKeyPath);
                ClientPrivateKey = (PrivateKey) RSA.ReadObjectFromFile(ClientprivateKeyPath);

                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean register() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please Enter Your Number:");
        setNumber(input.nextInt());
        System.out.println("Please Enter Your Password:");
        setPassword(input.next());
        try {
            SaveStream.sendString(outToServer, secretKey, this.api.getRegisterApi());
            //outToServer.writeUTF(this.api.getRegisterApi());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error In Api register");
            return false;
        }
        try {
            //outToServer.writeInt(getNumber());
            //outToServer.writeUTF(getPassword());

            SaveStream.sendString(outToServer, secretKey, String.valueOf(getNumber()));
            SaveStream.sendString(outToServer, secretKey, getPassword());
            try {
                RSA.generateKeys(ClientpublicKeyPath, ClientprivateKeyPath);
            } catch (Exception e) {
                System.out.println("Error In Generate Client Keys");
            }
            ClientPublicKey = (PublicKey) RSA.ReadObjectFromFile(ClientpublicKeyPath);
            ClientPrivateKey = (PrivateKey) RSA.ReadObjectFromFile(ClientprivateKeyPath);

            if (ClientPublicKey != null) {
                SaveStream.sendString(outToServer, secretKey, RSA.publicKeyToString(ClientPublicKey));
            }

            return inFromServer.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private int getNumber() {
        return number;
    }


    public boolean send() {
        Scanner input = new Scanner(System.in);
        Message message = new Message();
        if (numbers.size() == 0) {
            System.out.println("you don't have friend yet.....!" +
                    "\n please add new friend and try again ");
            return false;
        }
        for (int i = 0; i < numbers.size(); i++) {
            System.out.println(i + "..." + numbers.get(i).getKey());
        }
        int i = input.nextInt();
        if (i < 0 || i > numbers.size()) {
            return false;
        }
        message.setDestination(numbers.get(i).getKey());
        System.out.println("Enter the message's text:");
        String line = input.next();
        message.setText(line);
        message.setSender(number);

        try {
            SaveStream.sendString(outToServer, this.secretKey, this.api.getSendApi(), ClientPrivateKey);
            //SaveStream.sendString(outToServer,this.secretKey,this.api.getSendApi());
            //outToServer.writeUTF(this.api.getSendApi());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error In Api send ");
            return false;
        }
        try {
            String encrypt = RSA.encrypt(line, RSA.stringToPublicKey(numbers.get(i).getValue()));

            SaveStream.sendString(outToServer, this.secretKey, String.valueOf(message.getSender()), ClientPrivateKey);
            SaveStream.sendString(outToServer, this.secretKey, String.valueOf(message.getDestination()), ClientPrivateKey);
            SaveStream.sendString(outToServer, this.secretKey, encrypt, ClientPrivateKey);

            //SaveStream.sendString(outToServer,this.secretKey,String.valueOf(message.getSender()));
            //SaveStream.sendString(outToServer,this.secretKey,String.valueOf(message.getDestination()));
            //SaveStream.sendString(outToServer,this.secretKey,message.getText());
            return inFromServer.readBoolean();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }


    public boolean addFriend() {
        Scanner input = new Scanner(System.in);
        System.out.println("enter the number");
        int number = input.nextInt();
        try {
            SaveStream.sendString(outToServer, this.secretKey, this.api.getAddFriend(), ClientPrivateKey);
            //SaveStream.sendString(outToServer,this.secretKey,this.api.getAddFriend());
            //outToServer.writeUTF(this.api.getAddFriend());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error In Api send ");
            return false;
        }
        try {
            SaveStream.sendString(outToServer, this.secretKey, String.valueOf(number), ClientPrivateKey);
            //SaveStream.sendString(outToServer,this.secretKey,String.valueOf(number));
            return inFromServer.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return false;

    }


    void close() {
        try {
            SaveStream.sendString(outToServer, this.secretKey, api.getClose(), ClientPrivateKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void close1() {
        try {
            outToServer.writeUTF(api.getClose());

            //  SaveStream.sendString(outToServer,this.secretKey,api.getClose());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void continue_() {
        try {
            SaveStream.sendString(outToServer, this.secretKey, this.api.getContinueApi(), ClientPrivateKey);
            //outToServer.writeUTF(api.getContinueApi());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean receiveMessageFromServer() throws Exception {

        int size = inFromServer.readInt();
        boolean b = true;
        for (int i = 0; i < size; i++) {
            Pair<String, Boolean> strSend = SaveStream.receiveString(inFromServer, secretKey, serverPublicKey);
            Pair<String, Boolean> strDest = SaveStream.receiveString(inFromServer, secretKey, serverPublicKey);
            Pair<String, Boolean> strText = SaveStream.receiveString(inFromServer, secretKey, serverPublicKey);
            String decrypt = "null";
            decrypt = RSA.decrypt(strText.getKey(), this.ClientPrivateKey);

            //Pair<String,Boolean> strSend=SaveStream.receiveString(inFromServer,secretKey);
            //Pair<String,Boolean> strDest=SaveStream.receiveString(inFromServer,secretKey);
            //Pair<String,Boolean> strText=SaveStream.receiveString(inFromServer,secretKey);
            //int  send=inFromServer.readInt();
            //int  dest=inFromServer.readInt();
            //String  text=inFromServer.readUTF();
            messages.add(new Message(decrypt, Integer.parseInt(strDest.getKey()), Integer.parseInt(strSend.getKey())));
            b = b && strSend.getValue() && strDest.getValue() && strText.getValue();
        }
        return b;
    }


    void showMessages() {
        System.out.println("Messages:");
        for (Message s : messages) {
            System.out.println(s + "\n");
        }
        System.out.println();
    }


    void generateSecretKey() {
        this.secretKey = RSA.generateRandomString();
        System.out.println("secret key=" + secretKey);
    }

    void receivePublicKey() throws IOException {
        String publicKeyString = inFromServer.readUTF();
        this.serverPublicKey = RSA.stringToPublicKey(publicKeyString);
    }

    void sendSecretKey() throws Exception {
        String encryptSecretKey = RSA.encrypt(this.secretKey, this.serverPublicKey);
        outToServer.writeUTF(encryptSecretKey);
    }

    public void certificate() {

        Socket CASocket;

        String publicKeyPath = "ClinetPublicKeyss",
                privateKeyPath = "ClientPrivateKeyss";
        try {
            X509CertImpl cert = null;
            System.out.println("create cer");
            InetAddress adr = InetAddress.getByName(null);
            CASocket = new Socket(adr, 5999);
            RSA.generateKeys(publicKeyPath, privateKeyPath);
            cert = CertificateController.CAServerClient(CASocket, "cn=" + number + ", O=Chat", publicKeyPath, privateKeyPath);
            CertificateController.saveCert(cert, "");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public boolean receiveNumbersFromServer() throws IOException {
        numbers = new ArrayList<>();
        int size = inFromServer.readInt();
        boolean b = true;
        for (int i = 0; i < size; i++) {
            Pair<String, Boolean> strNumber = SaveStream.receiveString(inFromServer, secretKey, serverPublicKey);
            Pair<String, Boolean> strPublicKey = SaveStream.receiveString(inFromServer, secretKey, serverPublicKey);

            //Pair<String,Boolean> strNumber=SaveStream.receiveString(inFromServer,secretKey);
            //Pair<String,Boolean> strPublicKey=SaveStream.receiveString(inFromServer,secretKey);

            //int cNumber=inFromServer.readInt();
            //int  cPublic=inFromServer.readInt();

            numbers.add(new Pair<>(Integer.parseInt(strNumber.getKey()), strPublicKey.getKey()));
            b = b && strNumber.getValue() && strPublicKey.getValue();
        }
        return b;
    }


    void receiveCertfifcate() throws IOException, CertificateException, InvalidNameException {
        // Receiving data from server
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        baos.write(buffer, 0, inFromServer.read(buffer));
        byte[] result = baos.toByteArray();

        X509CertImpl cert = new X509CertImpl(result);
        String dn = cert.getSubjectX500Principal().getName();
        LdapName ldapDN = new LdapName(dn);
        String o = "";
        for (Rdn rdn : ldapDN.getRdns()) {
            System.out.println(rdn.getType() + " -> " + rdn.getValue());
            if (rdn.getType().equals("O")) {
                o = rdn.getValue().toString();
            }
        }
        if (CertificateController.verifyCert(cert) && o.equals("chat")) {
            this.serverPublicKey = cert.getPublicKey();
            System.out.println("good");
        } else {
            socket.close();
            throw new CertificateException();
        }
    }

}
