package pro.server;


import java.util.Date;

public class RequestApi {

    public static int message_Id;
    public static String requestApi;
    public static String encryptRequestApi;
    public static Date date;
    public static String apiKey;
    public static String DS;


    public static void setMessage_Id(int message_Id) {
        RequestApi.message_Id = message_Id;
    }

    public static void setRequestApi(String requestApi) {
        RequestApi.requestApi = requestApi;
    }

    public static void setEncryptRequestApi(String encryptRequestApi) {
        RequestApi.encryptRequestApi = encryptRequestApi;
    }

    public static void setDate(Date date) {
        RequestApi.date = date;
    }

    public static void setApiKey(String apiKey) {
        RequestApi.apiKey = apiKey;
    }

    public static void setDS(String DS) {
        RequestApi.DS = DS;
    }

    //////////////////// save Request ///////////////////////
    public static void saveRequest(){

        DataBase dataBase = new DataBase();
        dataBase.openConnection();
        dataBase.insertNewRequestApi(message_Id,requestApi,encryptRequestApi,date,apiKey,DS);
        dataBase.closeConnection();

    }
}
