package bdcc.auction;
import bdcc.kademlia.KeyNode;


public class Auction {
    String auction_id;
    String seller_id;
    String item;
    double value;
    Bid bid = null;

    public Auction(String a, String b, double c, String id){
        auction_id = id;
        seller_id = a;
        item = b;
        value = c; //starting value
    }

    public String getSeller(){
        return this.seller_id;
    }

    public String getItem(){
        return this.item;
    }

    public double getValue(){
        return this.value;
    }
    
    public double getHighestBid(){
        return this.bid.getInfoValue();
    }

    public KeyNode getHighestBidder(){
        return this.bid.getInfoBidder();
    }

    public boolean updateBid(Bid b){
        if(!(b.getInfoId() == this.auction_id)){
            return false;
        }

        if((this.bid != null) && (b.getInfoValue() > this.bid.getInfoValue()) ){
            this.bid = b;
            return true;
        }

        else if(b.getInfoValue() > this.value){
            this.bid = b;
            return true;
        }

        return false;
    }

}