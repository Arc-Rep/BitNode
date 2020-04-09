package bdcc.auction;


import bdcc.chain.Crypto;
import java.security.Key;
import java.util.Base64;


public class User{
    
    private String user_id;
    private String user_name;
    private double wallet;
    private Key privKey;
    Key pubKey;

    //BASE64Encoder b64 = new BASE64Encoder();

    public User(String username){
        user_name =     username;
        user_id =       Crypto.hashString(user_name);
        wallet =        0;
        Crypto.generateRSAKeys(pubKey, privKey);
    }

    private void add_amount(double amount_to_add){
        wallet+= amount_to_add;
    }

    public String getUserId(){
        return user_id;
    }
}