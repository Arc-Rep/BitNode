package bdcc.auction;

import bdcc.chain.Crypto;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.util.UUID;


public class User{
    
    //make an item inventory?
    private String user_id;
    private double wallet;
    private PrivateKey privKey;
    private Auction user_auction;
    PublicKey pubKey;

    //BASE64Encoder b64 = new BASE64Encoder();

    public User(){
        KeyPair pair = Crypto.generateRSAKeys();
        privKey = pair.getPrivate();
        pubKey = pair.getPublic();
        user_id =       Crypto.hashString(pubKey.toString());
        wallet =        0;
        user_auction = null;        
    }

    private void add_amount(double amount_to_add){
        wallet+= amount_to_add;
    }

    public String getUserId(){
        return user_id;
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

    public byte[] getPubKey(){
        return pubKey.getEncoded();
    }

    public byte[] getPrivateKey(){
        return privKey.getEncoded();
    }

    public boolean processBid(Bid new_bid){
        return user_auction.updateBid(new_bid);
    }

    public boolean checkActiveAuction(String auction_id){
        return auction_id.equals(user_auction.auction_id);
    }
}