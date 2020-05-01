package bdcc.auction;


public class Auction {
    String auction_id;
    String seller_id;
    String item;
    double value;
    Bid bid;

    public Auction(String seller, String item_name, double c, String id, Bid new_bid){
        auction_id = id;
        seller_id = seller;
        item = item_name;
        value = c; //starting value
        bid = new_bid;
    }

    public Auction(String seller, String item_name, double c, String id){
        auction_id = id;
        seller_id = seller;
        item = item_name;
        value = c; //starting value
    }

    public String getAuctionId(){
        return this.auction_id;
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
        if(this.bid == null) return -1;

        return this.bid.getInfoValue();
    }

    public String getHighestBidder(){
        if(this.bid == null) return null;

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

    public boolean compareAuctionId(String id){
        if(this.auction_id.equals(id)){
            return true;
        }
        return false;
    }

}