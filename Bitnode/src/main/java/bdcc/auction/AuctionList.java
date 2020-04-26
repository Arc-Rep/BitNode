package bdcc.auction;

import java.util.HashMap;
import java.util.UUID;
import java.util.Random;

import bdcc.kademlia.KeyNode;

public class AuctionList {

    HashMap<String,Auction> auction_list;
    HashMap<String,Auction> completed_list;

    public AuctionList(){
        auction_list = new HashMap<String,Auction>();
        completed_list = new HashMap<String,Auction>();
    }

    public synchronized void newAuction(String a, String b, double c){
        String id;
        do{
            id = UUID.randomUUID().toString();
        }while(auction_list.containsKey(id));
        Auction au = new  Auction(a,b,c,id);
        auction_list.put(id,au);
    }

    public synchronized Auction getInfoById(String id){
        if(auction_list.containsKey(id)){
            return auction_list.get(id);
        }
        else{
            return null;
        }
    }

    public synchronized boolean completeAuction(String id, String uid){
        if(auction_list.containsKey(id) && (!completed_list.containsKey(id))){
            Auction temp = auction_list.get(id);
            if(temp.getSeller() == uid){
                completed_list.put(id, temp);
                auction_list.remove(id);
                return true;
            }  
        }
       return false;
    }

    public synchronized int makeBid(String id, double value, KeyNode bidder){ // 1 - accepted, 2 - rejected, 3 - auction not found 
        if(!auction_list.containsKey(id) || completed_list.containsKey(id)){
            return 3;
        }

        Auction au = auction_list.get(id);
        Bid bid = new Bid(id, value, bidder);

        if(au.updateBid(bid)){
            return 1;
        }
        else{
            return 2;
        }
    }

    /*public synchronized Auction getRandomAuction(){
        if(auction_list.size() == 0) return null;
        Random r = new Random();
        return auction_list.get(r.nextInt() % auction_list.size());
    }*/

}