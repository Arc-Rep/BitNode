package bdcc.grpc;

import java.util.Iterator;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import bdcc.chain.Crypto;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader; 
import java.io.IOException; 
import java.io.InputStreamReader; 
import java.io.Reader;
import io.grpc.stub.StreamObserver;

public class NodeOperationsClient {
    private static final Logger logger = Logger.getLogger(NodeOperationsClient.class.getName());
  
    private final ManagedChannel channel;
    private final NodeOperationsGrpc.NodeOperationsBlockingStub blockingStub;
    private final NodeOperationsGrpc.NodeOperationsStub asyncStub;
    /** Construct client connecting to GrpcFlow server at {@code host:port}. */
    public NodeOperationsClient(String host, int port) {
      channel = ManagedChannelBuilder.forAddress(host, port)
          .usePlaintext()
          .build();
      blockingStub = NodeOperationsGrpc.newBlockingStub(channel);
      asyncStub = NodeOperationsGrpc.newStub(channel);
    }
    
    public void shutdown() throws InterruptedException {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public String registerNode(String node_address){ //returns node id
      String id = "";
      try
      {
        NodeAddress address = NodeAddress.newBuilder().setNodeAddress(node_address).build();

        NodeInfo response = blockingStub.registerNode(address);

        id = response.getUserId();
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }

      return id;
    }

    public NodeNotification notifyNode(String user_id, String user_address, String user_public_key,
                                       String auction_id, String item, String max_bid, String random_auction_id,
                                       String random_user_id, String random_item, String random_max_bid,
                                       String auction_buyer, String random_auction_buyer){
      NodeNotification response = null;
      try
      {

        NodeNotification inforequest = 
          NodeNotification.newBuilder().setUserId(user_id).setUserAddress(user_address).
          setPublicKey(user_public_key).setAuctionId(auction_id).setItem(item).setMaxBid(max_bid).
          setRandomAuctionId(random_auction_id).setRandomUserId(random_user_id).
          setRandomItem(random_item).setRandomMaxBid(random_max_bid).
          setAuctionBuyer(auction_buyer).setRandomAuctionBuyer(random_auction_buyer).
          build();

        response = blockingStub.notifyNode(inforequest); 
      } catch (RuntimeException e) {
        System.out.println("RPC Error: Failed to establish communication with server on notify");
      } catch (Exception e) {

      }
      return response;
    }

    public Iterator<NodeSecInfo> findNode(String user_id, String user_address, String user_public_key){
      Iterator<NodeSecInfo> response = null;
      try
      {
        NodeSecInfo inforequest = NodeSecInfo.newBuilder().setUserId(user_id)
          .setUserAddress(user_address).setPublicKey(user_public_key).build();
        response = blockingStub.findNode(inforequest); 
      } catch (RuntimeException e) {
        System.out.println("RPC Error: Failed to establish communication with server on findNode");
      }
      return response;
    }

    public Iterator<NodeSecInfo> lookupNode(String user_id, String user_address, String pub_key){
      Iterator<NodeSecInfo> response = null;

      try
      {
        NodeSecInfo inforequest = NodeSecInfo.newBuilder().setUserId(user_id).setUserAddress(user_address).
                                setPublicKey(pub_key).build();
        response = blockingStub.lookupNode(inforequest); 
      } catch (RuntimeException e) {
        System.out.println("RPC Error: Failed to establish communication with server on lookup");
      }
      return response;
    }  

    public NodeResponse makeTransaction(String buyer_id, String amount, String seller_id){
      NodeResponse response = null;
      try
      {
        TransactionInfo inforequest = TransactionInfo.newBuilder()
          .setBuyerId(buyer_id)
          .setAmount(amount)
          .setSellerId(seller_id)
          .build();
        response = blockingStub.makeTransaction(inforequest);
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "RPC failed", e);
      }
      return response;
    }

    public InfoAuction infoAuction(String user_id, String user_address, String user_public_key){
      InfoAuction response = null;
      try{
        NodeSecInfo infoauctionrequest = NodeSecInfo.newBuilder().setUserId(user_id)
            .setUserAddress(user_address).setPublicKey(user_public_key).build();
        response = blockingStub.infoAuction(infoauctionrequest);
      } catch(RuntimeException e){
        logger.log(Level.WARNING, "RPC failed", e);
      }
      return response;
    }

    public NodeResponse makeBid(String buyer_id, String auction_id, String amount){
      NodeResponse response;
      try{
        MakeBid bidrequest = MakeBid.newBuilder()
          .setBuyerId(buyer_id)
          .setAuctionId(auction_id)
          .setAmount(amount)
          .build();
        response = blockingStub.makeBid(bidrequest);
      } catch(RuntimeException e){
        logger.log(Level.WARNING, "RCP failed", e);
        return null;
      }
      return response;
    }

    public TransactionInfo resultsAuction(String auction_id, String buyer_id, String value, String seller_id, 
      String seller_address, String seller_public_key){
      TransactionInfo response = null;
      try{
        ResultsAuction resutltsauctionrequest = ResultsAuction.newBuilder()
          .setAuctionId(auction_id)
          .setBuyerId(buyer_id)
          .setValue(value)
          .setSellerId(seller_id)
          .setSellerAddress(seller_address)
          .setSellerPublicKey(seller_public_key)
          .build();
        response = blockingStub.resultsAuction(resutltsauctionrequest);
      } catch(RuntimeException e){
        logger.log(Level.WARNING, "RCP failed", e);
      }
      return response;
    }
   
}
  