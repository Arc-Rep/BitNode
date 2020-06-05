package bdcc.auction;

import java.util.concurrent.CopyOnWriteArrayList;

import bdcc.chain.Crypto;

import java.util.Random;

public class AuctionList {

    CopyOnWriteArrayList<Auction> live_list;
    CopyOnWriteArrayList<Auction> completed_list;
    CopyOnWriteArrayList<Auction> adopted_list;

    public AuctionList(){
        live_list = new CopyOnWriteArrayList<Auction>();
        completed_list = new CopyOnWriteArrayList<Auction>();
        adopted_list = new CopyOnWriteArrayList<Auction>();
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

    public synchronized Auction getAdoptedAuctionById(String id){
        for (Auction auction : adopted_list) {
            if(auction.getAuctionId().equals(id)){
                return auction;
            }
        }
        return null;
    }

    public Auction getAuctionByIndex(int index){
        if(live_list.size() > index) return live_list.get(index);
        return null;
    }

    public void removeAuction(String auction_id){    //return auction if adopted
        for (Auction auction : live_list) {
            if(auction.getAuctionId().equals(auction_id)){
                live_list.remove(auction);
                return;
            }
        }
    }

    public Auction removeAdoptedAuction(String auction_id){ 
        for (Auction auction : adopted_list) {
            if(auction.getAuctionId().equals(auction_id)){
                adopted_list.remove(auction);
                return auction;
            }
        }
        return null;
    }

    public synchronized void getLiveAuctions(){
        int index = 1;
        for (Auction au : live_list) {
            System.out.println("=============================================");
            System.out.println("Auction Number " + index + ":");
            System.out.println("Auction ID: " + Crypto.toHex(au.getAuctionId()));
            System.out.println("    - Item: " + au.getItem());
            if(au.getHighestBid() == -1){
                System.out.println("    - Value: " + au.getValue());
            }else {
                System.out.println("    - Highest Bid: " + au.getHighestBid());
                System.out.println("    - Bidder: " + au.getHighestBidder());
            }
            System.out.println("=============================================");
            index++;
        }
    }

    public synchronized void updateList(String old_auction_id, Auction auction_to_add, int i){ //fazer verificações?
        Auction old_auction = getAuctionById(old_auction_id);

        if(i==1){ //new bid
            if(old_auction != null) live_list.remove(old_auction);
            live_list.add(auction_to_add);
        } else if(i==2){ // completed
            live_list.remove(old_auction);
            completed_list.add(auction_to_add);
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

    public synchronized void addToAdoptedAuctionList(Auction to_add){
        for (Auction auction : adopted_list) {
            if(auction.getAuctionId().equals(to_add.getAuctionId())){
                adopted_list.remove(auction);
                adopted_list.add(to_add);
                return;
            }
        }
        adopted_list.add(to_add);
    }

    public synchronized Auction getRandomAuction(){
        if(live_list.size() == 0) return null;
        Random r = new Random();
        int random_index = r.nextInt() % live_list.size();
        if(random_index >= live_list.size()) return null;
        return live_list.get(random_index);
    }

    public Boolean checkLiveAuctionExists(){
        if(live_list.size() == 0) return false;
        return true;
    }

    public Boolean enterAdoptedAuction(Auction auction, User user){
        if(user.getWallet() < auction.getHighestBid()) return false;
        user.vaultAmount(auction.getHighestBid());
        adopted_list.add(auction);
        return true;
    }

    public void removeAuctionsOfNode(String node_id, User user){
        for (Auction auction : live_list) {
            if(auction.getSeller().equals(node_id)){
                live_list.remove(auction);
                break;
            }
        }

        for (Auction auction : adopted_list) {
            if(auction.getSeller().equals(node_id)){
                adopted_list.remove(auction);
                user.returnMoney(auction.getCurrentHighestAmount());
                break;
            }
        }

    }


}