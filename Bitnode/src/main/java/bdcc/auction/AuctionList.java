package bdcc.auction;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;

public class AuctionList {

    CopyOnWriteArrayList<Auction> live_list;
    CopyOnWriteArrayList<Auction> completed_list;

    public AuctionList(){
        live_list = new CopyOnWriteArrayList<Auction>();
        completed_list = new CopyOnWriteArrayList<Auction>();
    }

    public synchronized boolean auctionIsLive(String id){
        for (Auction auction : live_list) {
            if(auction.getAuctionId().equals(id)){
                return true;
            }
        }
        return false;
    }

    public synchronized Auction getAuctionById(String id){
        for (Auction auction : live_list) {
            if(auction.getAuctionId().equals(id)){
                return auction;
            }
        }
        return null;
    }

    public synchronized void getLiveAuctions(){
        for (Auction au : live_list) {
            System.out.println("=============================================");
            System.out.println("Auction ID: " + au.getAuctionId());
            System.out.println("    - Item: " + au.getItem());
            if(au.getHighestBid() == -1){
                System.out.println("    - Value: " + au.getValue());
            }else {
                System.out.println("    - Highest Bid: " + au.getHighestBid());
                System.out.println("    - Bidder: " + au.getHighestBidder());
            }
            System.out.println("=============================================");
        }
    }

    public synchronized void updateList(Auction au, int i){ //fazer verificações?
        if(i==1){ //new bid
            live_list.remove(au);
            live_list.add(au);
        } else if(i==2){ // completed
            completed_list.add(au);
        }
        
    }

    public synchronized void addToAuctionList(Auction to_add){
        for (Auction auction : live_list) {
            if(auction.getAuctionId().equals(to_add.getAuctionId())){
                live_list.remove(auction);
                live_list.add(to_add);
                return;
            }
        }
        live_list.add(to_add);
    }

    public synchronized Auction getRandomAuction(){
        if(live_list.size() == 0) return null;
        Random r = new Random();
        return live_list.get(r.nextInt() % live_list.size());
    }

}