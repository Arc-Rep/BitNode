package bdcc.auction;

import bdcc.chain.Crypto;
import java.security.Key;
import java.util.UUID;


public class User{
    
    //make an item inventory?
    private String user_id;
    private String user_name;
    private double wallet;
    private Key privKey;
    private Auction user_auction;
    Key pubKey;

    //BASE64Encoder b64 = new BASE64Encoder();

    public User(String username){
        user_name =     username;
        user_id =       Crypto.hashString(user_name);
        wallet =        0;
        user_auction = null;
        Crypto.generateRSAKeys(pubKey, privKey);
    }

    private void add_amount(double amount_to_add){
        wallet+= amount_to_add;
    }

    public String getUserId(){
        return user_id;
    }

    public String getUserName(){
        return user_name;
    }

    public Auction getUserAuction(){
        return user_auction;
    }

    public double getWallet(){
        return this.wallet;
    }

    public boolean setUpAuction(String b, double c){
        if(user_auction == null){
            String id = Crypto.hashString(UUID.randomUUID().toString() + this.user_id);
            this.user_auction = new Auction(this.user_id, b, c, id, null);
            return true;
        }
        return false;
    }

    public void concludeAuction(){
        //do not forget to make the transefer
        this.user_auction = null;
    }
}