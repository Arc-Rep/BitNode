package bdcc.auction;

import bdcc.kademlia.KeyNode;

public class Bid {
    String auction_id;
    double value;
    KeyNode bidder;

    public Bid(String a, double b, KeyNode c){
        this.auction_id = a;
        this.value = b;
        this.bidder = c;
    }

    public String getInfoId(){
        return this.auction_id;
    }

    public double getInfoValue(){
        return this.value;
    }
    
    public KeyNode getInfoBidder(){
        return this.bidder;
    }
}



