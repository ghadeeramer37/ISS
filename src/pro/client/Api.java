package pro.client;

public class Api {

    private final String loginApi = "login";
    private final String registerApi = "register";
    private  final String sendApi="send";
    private final String addFriend="addFriend";
    private final String continueApi="continueApi";
    private final String close="close";


    public String getLoginApi() {
        return loginApi;
    }

    public String getRegisterApi() {
        return registerApi;
    }

    public String getSendApi() {
        return sendApi;
    }

    public String getContinueApi() {
        return continueApi;
    }

    public String getClose() {
        return close;
    }

    public String getAddFriend() {
        return addFriend;
    }
}
