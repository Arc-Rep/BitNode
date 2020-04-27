package bdcc.auction;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;

public class AuctionList {

    CopyOnWriteArrayList<Auction> auction_list;
    CopyOnWriteArrayList<Auction> completed_list;

    public AuctionList(){
        auction_list = new CopyOnWriteArrayList<Auction>();
        completed_list = new CopyOnWriteArrayList<Auction>();
    }

    public synchronized Auction auctionIsLive(String id){
        for(int i = 0; i < auction_list.size(); i++){
            if(auction_list.get(i).compareAuctionId(id)){
                return auction_list.get(i);
            }
        }
        return null;
    }

    public synchronized Auction getAuctionById(String id){
        for (Auction auction : auction_list) {
            if(auction.getAuctionId().equals(id)){
                return auction;
            }
        }
        return null;
    }

    public synchronized void updateList(Auction au, int i){ //fazer verificações?
        if(i==1){ //new bid
            auction_list.remove(au);
            auction_list.add(au);
        } else { // completed
            completed_list.add(au);
        }
        
    }

    public synchronized void addToAuctionList(Auction to_add){
        for (Auction auction : auction_list) {
            if(auction.getAuctionId().equals(to_add.getAuctionId())){
                auction_list.remove(auction);
                auction_list.add(to_add);
                return;
            }
        }
        auction_list.add(to_add);
    }

    
    public synchronized Auction getRandomAuction(){
        if(auction_list.size() == 0) return null;
        Random r = new Random();
        return auction_list.get(r.nextInt() % auction_list.size());
    }

}