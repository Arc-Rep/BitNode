package bdcc.auction;

import bdcc.chain.Crypto;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.util.UUID;


public class User{
    
    private String user_id;
    private double wallet;
    private double vaulted_amount;
    private PrivateKey privKey;
    private Auction user_auction;
    PublicKey pubKey;

    public User(String id){
        KeyPair pair = Crypto.generateRSAKeys();
        privKey = pair.getPrivate();
        pubKey = pair.getPublic();
        user_id =       id;
        wallet =        10;
        user_auction = null;  
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

    public double getVaulted(){
        return this.vaulted_amount;
    }

    public Boolean vaultAmount(double to_vault){
        if(to_vault > wallet) return false;
        wallet -= to_vault;
        vaulted_amount += to_vault;
        return true;
    }

    public Boolean directPayment(double to_pay){
        if(to_pay > wallet) return false;
        wallet -= to_pay;
        return true;
    }
    

    public Boolean returnMoney(double to_return){
        if(to_return > vaulted_amount) return false;
        wallet += to_return;
        vaulted_amount -= to_return;
        return true;
    } 

    public void withdrawAmount(double amount_to_withdraw){
        vaulted_amount-=amount_to_withdraw;
    }

    public void receiveMoneyTransfer(double amount_to_receive){
        wallet += amount_to_receive;
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
        this.user_auction = null;
    }

    public Boolean proccessAuctionConclusion(String seller, String buyer, Double amount){
        Boolean success = false;
        if(buyer.equals(user_auction.getHighestBidder()) && seller.equals(getUserId()) 
            && amount == user_auction.getHighestBid())
            success = true;
        
        return success;
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