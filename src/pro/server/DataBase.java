package pro.server;

import javafx.util.Pair;
import pro.Encription.RSA;

import java.security.PublicKey;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class DataBase {
    private String userName;
    private String password;
    private String host ;
    private Connection connection;

    public DataBase() {

        host = "jdbc:mysql://localhost:3306/chat";
        userName= "root";
        password="";
        connection=null;
    }

    public void openConnection(){
        try{
            //Open a connection
            //DriverManager.registerDriver(new com.mchange.sqlserver.jdbc.SQLServerDriver());
            connection = DriverManager.getConnection(host, userName, password);
        }catch(Exception e){
            //Handle errors for JDBC
            e.printStackTrace();
        }
    }

    public void closeConnection(){

        try{
            if(connection!=null)
                connection.close();
        }catch(SQLException se){
            se.printStackTrace();
        }
    }

    public boolean userIsExist(int number){
        Statement stm = null;
        boolean b=false;
        try {
            stm = connection.createStatement();
            ResultSet set = stm.executeQuery(String.format("select * from usres as U where U.number = '%d'",number));
            b = set.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return b;
    }

    public String userColumns() {
        return "(number,password)";
    }

    public String userValues(int number, String password) {
        return String.format("('%d','%s')",number,password);
    }


    public String messageColumns() {
        return "(senderNumber,destNumber,text)";
    }

    public String messageValues(int senderNumber,int destNumber,String text) {
        return String.format("('%d','%d','%s')",senderNumber,destNumber,text);
    }

    public void saveUser(int number, String password) {
        try {
            Statement stm = null;
            stm = connection.createStatement();
            String values = userValues(number, password);
            String sqlStm = String.format("insert into usres %s values %s",userColumns(),values);

            stm.execute(sqlStm);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public int getUserId(int number, String password){
        Statement stm = null;
        int id=-1;
        try {
            stm = connection.createStatement();
            String sqlQuary=String.format("select * from usres as U where U.number = '%d' and U.password like '%s'",number,password);
            ResultSet set = stm.executeQuery(sqlQuary);

            if(set.next())
                id= set.getInt("id");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public ArrayList<message> getAllItemsMessages(int number) {
        Statement stm = null;
        /*int number=-1;
        try {
            String sqlQuary=String.format("select * from usres where id ='%d'",user_id);
            ResultSet set = stm.executeQuery(sqlQuary);
            if(set.next())
                 number=set.getInt("number");
        } catch (SQLException e) {
            e.printStackTrace();
        }
*/
        ArrayList<message>Items=new ArrayList<message>();
        try {
            stm = connection.createStatement();
            String sqlQuary=String.format("select * from messages where destNumber ='%d'",number);
            ResultSet set = stm.executeQuery(sqlQuary);
            while (set.next()){
                message s=new message(set.getString("text"),set.getInt("destNumber"),set.getInt("senderNumber"));
                Items.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Items;
    }

    private int getDestNumber(int user_id) {

        Statement stm = null;
        int number=-1;
        try {
            stm = connection.createStatement();
            String sqlQuary=String.format("select * from usres where id ='%d'",user_id);
            ResultSet set = stm.executeQuery(sqlQuary);
            number=set.getInt("number");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return number;

    }

    public void addMessage(message m) {
        try {
            Statement stm = null;
            stm = connection.createStatement();
            String values = messageValues(m.getSender(),m.getDestination(),m.getText());
            String sqlStm = String.format("insert into messages %s values %s",messageColumns(),values);

            stm.execute(sqlStm);

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void test() {
        message m=new message("hi",45,35);
        try {
            Statement stm = null;
            stm = connection.createStatement();
            String values = messageValues(m.getSender(),m.getDestination(),m.getText());
            String sqlStm = String.format("insert into messages %s values %s",messageColumns(),values);

            stm.execute(sqlStm);

        } catch (SQLException e) {
            e.printStackTrace();
        }



    }

    public PublicKey getClientPublicKey(int user_id){
        Statement stm = null;
        PublicKey pu;
        try {
            stm = connection.createStatement();
            String sqlQuary = String.format("select public_key from usres where id = '%d'", user_id);
            ResultSet s =  stm.executeQuery(sqlQuary);
            while (s.next()){
                pu = RSA.stringToPublicKey(s.getString("public_key"));
                return pu;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }


    public String userColumnsWithKey() {
        return "(number,password,public_key)";
    }

    public String userValuesWithKey(int number, String password, String ClientPublicKey) {
        return String.format("('%d','%s','%s')",number,password,ClientPublicKey);
    }

    public void saveUser(int number, String password, String clintPublicKey) {

        try {
            Statement stm = null;
            stm = connection.createStatement();
            String values = userValuesWithKey(number, password,clintPublicKey);
            String sqlStm = String.format("insert into usres %s values %s",userColumnsWithKey(),values);

            stm.execute(sqlStm);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertNewRequestApi(int message_Id, String RA, String ERA, Date d, String AK, String DS){

        try {
            String sqlStm = "INSERT INTO request(message_Id,request_api,encrypt_request_api,date,api_key,DS)" +
                    " VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sqlStm);
            pstmt.setInt(1, message_Id);
            pstmt.setString(2, RA);
            pstmt.setString(3, ERA);
            pstmt.setDate(4, new java.sql.Date(d.getTime()));
            pstmt.setString(5,AK);
            pstmt.setString(6,DS);
            pstmt.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }


    public int getMessageId(message m) {

        Statement stm = null;
        int id=-1;
        try {
            stm = connection.createStatement();
            String sqlQuary=String.format("select * from messages as M where M.senderNumber = '%d' and M.destNumber = '%d' and M.text like '%s'",m.getSender(),m.getDestination(),m.getText());
            ResultSet set = stm.executeQuery(sqlQuary);

            if(set.next())
                id= set.getInt("id");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;

    }


    public ArrayList<Pair<Integer,String>> getAllNumbers(int user_id) {
        Statement stm = null;
        ArrayList<Pair<Integer,String>> Items=new ArrayList<>();
        try {
            stm = connection.createStatement();
            String sqlQuary=String.format("select * from friend where number2 ='%d'",user_id);
            ResultSet set = stm.executeQuery(sqlQuary);
            while (set.next()){
                //message s=new message(set.getString("text"),set.getInt("destNumber"),set.getInt("senderNumber"));
                Pair<Integer,String> p=new Pair<>(set.getInt("number1"),set.getString("public_Key"));
                Items.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Items;
    }



    public String friendColumns() {
        return "(number1,number2,public_Key)";
    }

    public String friendValues(int number1, int number2,String publicKey) {
        return String.format("('%d','%d','%s')",number1,number2,publicKey);
    }

    public void addFriend(int user_number,int  number) {
        Statement stm = null;
        String publicKey="";

        try {
            stm = connection.createStatement();
            String sqlQuary=String.format("select * from usres where number ='%d'",number);
            ResultSet set = stm.executeQuery(sqlQuary);
            if(set.next())
                publicKey=set.getString("public_key");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {

            stm = connection.createStatement();
            String values = friendValues(number,user_number,publicKey);
            String sqlStm = String.format("insert into friend %s values %s",friendColumns(),values);

            stm.execute(sqlStm);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PublicKey getClientPublicKeyByNumber(int user_number) {

        Statement stm = null;
        PublicKey pu;
        try {
            stm = connection.createStatement();
            String sqlQuary = String.format("select public_key from usres where number = '%d'", user_number);
            ResultSet s =  stm.executeQuery(sqlQuary);
            while (s.next()){
                pu = RSA.stringToPublicKey(s.getString("public_key"));
                return pu;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }
}
