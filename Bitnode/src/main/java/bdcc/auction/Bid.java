package bdcc.auction;

public class Bid {
    String auction_id;
    double value;
    String bidder;

    public Bid(String a, double b, String c){
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
    
    public String getInfoBidder(){
        return this.bidder;
    }
}



