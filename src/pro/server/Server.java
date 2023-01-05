package pro.server;

import javafx.util.Pair;
import pro.Encription.RSA;
import pro.Encription.SaveStream;
import sun.security.x509.X509CertImpl;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;

public class Server extends Thread{

    private final X509CertImpl cert;
    private DataOutputStream outToClient;
    private DataInputStream inFromClient;
    private Socket socket;
    private DataBase dataBase;
    private int User_id;
    private int User_Number;
    private String secretKey;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public Server(Socket socket) throws IOException {
        this.socket = socket;
        publicKey=(PublicKey) RSA.ReadObjectFromFile("PublicKey");
        privateKey=(PrivateKey)RSA.ReadObjectFromFile("PrivateKey");
        inFromClient = new DataInputStream(socket.getInputStream());
        outToClient = new DataOutputStream(socket.getOutputStream());
        dataBase=new DataBase();
        this.cert=null;
    }

    public Server(Socket socket, X509CertImpl cert) throws IOException {

        publicKey=(PublicKey) RSA.ReadObjectFromFile("PublicKey");
        privateKey=(PrivateKey)RSA.ReadObjectFromFile("PrivateKey");
        this.cert = cert;
        inFromClient = new DataInputStream(socket.getInputStream());
        outToClient = new DataOutputStream(socket.getOutputStream());
        dataBase=new DataBase();

    }


    public void run() {

        String api = null;
        /*try {
            api = inFromClient.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try {
            sendCertificateToClient();
            //sendPublicKeyToClient();
            receiveSecretKey();
            System.out.println("secret key="+secretKey);
            Pair<String,Boolean> sb=SaveStream.receiveString(inFromClient,secretKey);
            api = sb.getKey();
            if(!sb.getValue())
                api="close";
        } catch (IOException | CertificateEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(api);
        boolean login=false;
        if ("login".equals(api)) {
            try {
                //int number = inFromClient.readInt();
                //String password = inFromClient.readUTF();
                //setSecretKey(password);
                Pair<String,Boolean> numberPair=SaveStream.receiveString(inFromClient,secretKey);
                int number =Integer.parseInt(numberPair.getKey());
                Pair<String,Boolean> passswordPair=SaveStream.receiveString(inFromClient,secretKey);
                String password = passswordPair.getKey();
                login=login(number, password);
                outToClient.writeBoolean(login&&numberPair.getValue()&&passswordPair.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("register".equals(api)) {
            try {
                //int number = inFromClient.readInt();
                //String password = inFromClient.readUTF();
                //setSecretKey(password);
                Pair<String,Boolean> numberPair=SaveStream.receiveString(inFromClient,secretKey);
                int number =Integer.parseInt(numberPair.getKey());
                Pair<String,Boolean> passswordPair=SaveStream.receiveString(inFromClient,secretKey);
                String password = passswordPair.getKey();
                Pair<String,Boolean> clientPublicKeyPair=SaveStream.receiveString(inFromClient,secretKey);
                String clientPublicKey = clientPublicKeyPair.getKey();
                login=register(number, password,clientPublicKey);
                 outToClient.writeBoolean(login&&numberPair.getValue()&&passswordPair.getValue()&&clientPublicKeyPair.getValue());
               // login=register(number, password);
               // outToClient.writeBoolean(login&&numberPair.getValue()&&passswordPair.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!login){
            System.out.print("can not login or register\n\n");
            return;
        }

        try {
            api = SaveStream.receiveApiString(inFromClient,secretKey,User_id).getKey();
            //api=SaveStream.receiveString(inFromClient,secretKey).getKey();
            //api=inFromClient.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }



        if(api.equals("continueApi")){
            System.out.println("user :"+User_id+" in");
            try {
                dataBase.openConnection();
                sendNumbersToClient(dataBase.getAllNumbers(User_Number));
                sendMessagesToClient(dataBase.getAllItemsMessages(User_Number));
                dataBase.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while (!api.equals("close")){
            try {
                api=SaveStream.receiveString(inFromClient,secretKey,User_id).getKey();
                //api = inFromClient.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if ("addFriend".equals(api)) {
                try {
                    System.out.println(api);
                    outToClient.writeBoolean(addFriend());
                    try {
                        dataBase.openConnection();
                        sendNumbersToClient(dataBase.getAllNumbers(User_Number));
                        dataBase.closeConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if ("send".equals(api)) {
                System.out.println(api);
                RequestApi.setRequestApi(api);
                message m = null;
                try {
                    m = receiveItemFromClient();
                    outToClient.writeBoolean(addMessage(m));
                    dataBase.openConnection();
                    int m_id=dataBase.getMessageId(m);
                    dataBase.closeConnection();
                    RequestApi.setMessage_Id(m_id);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                RequestApi.saveRequest();

            }
        }
        try {
            System.out.println("user "+User_id+" out");
            inFromClient.close();
            outToClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    void sendCertificateToClient() throws IOException, CertificateEncodingException {
        outToClient.write(cert.getEncoded());
    }
    private boolean addFriend() throws IOException {


        //phase 3
        Pair<String,Boolean> number=SaveStream.receiveString(inFromClient,this.secretKey,User_id);


        //phase 2
        //Pair<String,Boolean> number=SaveStream.receiveString(inFromClient,this.secretKey);

        //phase 1
        //int number=inFromClient.readInt();

        dataBase.openConnection();
        dataBase.addFriend(User_Number,Integer.parseInt(number.getKey()));
        dataBase.closeConnection();

        return true;// number.getValue();
    }


    boolean login(int number,String password){
        boolean b=false;
        dataBase.openConnection();
        System.out.println("User Number: " + number + "\nPassword:  " + password);
        if((this.User_id=dataBase.getUserId(number,password))!=-1){
            b=true;
            User_Number=number;
        }
        dataBase.closeConnection();
        return b;
    }

    boolean register(int number,String password,String clintPublicKey){
        boolean b=false;
        dataBase.openConnection();
        System.out.println("User Name: " + number + "\nPassword:  " + password+ "\nClientPublicKey: " + clintPublicKey);
        if (!dataBase.userIsExist(number)) {
            b=true;
            dataBase.saveUser(number,password,clintPublicKey);
            this.User_id=dataBase.getUserId(number,password);
            this.User_Number=number;
        }
        dataBase.closeConnection();
        return b;
    }

    //phase 1
/*
    boolean register(int number,String password){
        boolean b=false;
        dataBase.openConnection();
        System.out.println("User Name: " + number + "\nPassword:  " + password);
        if (!dataBase.userIsExist(number)) {
            b=true;
            dataBase.saveUser(number,password);
            this.User_id=dataBase.getUserId(number,password);
        }
        dataBase.closeConnection();
        return b;
    }
*/



    boolean addMessage(message m){
        dataBase.openConnection();
        dataBase.addMessage(m);
        dataBase.closeConnection();
        return true;

    }

    message receiveItemFromClient() throws IOException {
        message m=new message();
        //phase 3
        Pair<String,Boolean> Sender=SaveStream.receiveString(inFromClient,this.secretKey,User_id);
        Pair<String,Boolean> Destination=SaveStream.receiveString(inFromClient,this.secretKey,User_id);
        Pair<String,Boolean> Text=SaveStream.receiveString(inFromClient,this.secretKey,User_id);
        //String text=inFromClient.readUTF();

        //phase 2
        //Pair<String,Boolean> Sender=SaveStream.receiveString(inFromClient,this.secretKey);
        //Pair<String,Boolean> Destination=SaveStream.receiveString(inFromClient,this.secretKey);
        //Pair<String,Boolean> Text=SaveStream.receiveString(inFromClient,this.secretKey);

        //phase 1
        //i.setSender(inFromClient.readInt());
        //i.setDestination(inFromClient.readInt());
        //i.setText(inFromClient.readUTF());

        m.setSender(Integer.parseInt(Sender.getKey()));
        m.setDestination(Integer.parseInt(Destination.getKey()));
        m.setText(Text.getKey());
        //m.setText(text);
        m.setCorrect(Sender.getValue()&&Destination.getValue()&&Text.getValue());
        return m;
    }

    void sendMessagesToClient(ArrayList<message> meessages) throws IOException {
        outToClient.writeInt(meessages.size());
        for (message m: meessages) {

            SaveStream.sendString(outToClient,secretKey,String.valueOf(m.getSender()),privateKey);
            SaveStream.sendString(outToClient,secretKey,String.valueOf(m.getDestination()),privateKey);
            SaveStream.sendString(outToClient,secretKey,m.getText(),privateKey);


           // outToClient.writeInt(m.getSender());
           // outToClient.writeInt(m.getDestination());
           // outToClient.writeUTF(m.getText());
        }
    }



    void sendNumbersToClient(ArrayList<Pair<Integer,String>> numbers) throws IOException {
        outToClient.writeInt(numbers.size());

        for(int i=0;i<numbers.size();i++){
            SaveStream.sendString(outToClient,secretKey,String.valueOf(numbers.get(i).getKey()),privateKey);
            SaveStream.sendString(outToClient,secretKey,numbers.get(i).getValue(),privateKey);

            // outToClient.writeInt(m.getSender());
            // outToClient.writeInt(m.getDestination());
            // outToClient.writeUTF(m.getText());
        }
    }


    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }


    void sendPublicKeyToClient() throws IOException {
        String publicKey= RSA.publicKeyToString(this.publicKey);
        outToClient.writeUTF(publicKey);
    }

    void receiveSecretKey() throws IOException {
        String encryptSecretKey= inFromClient.readUTF();
        try {
            this.setSecretKey(RSA.decrypt(encryptSecretKey,privateKey));
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

}
