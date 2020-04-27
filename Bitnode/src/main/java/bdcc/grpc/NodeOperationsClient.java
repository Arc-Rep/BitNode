package bdcc.grpc;

import java.util.Iterator;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public NodeNotification notifyNode(String user_id, String user_address, String auction_id,
                                       String item, double max_bid, String random_auction_id,
                                       String random_user_id, String random_item, double random_max_bid){
      NodeNotification response = null;
      try
      {
        NodeNotification inforequest = 
          NodeNotification.newBuilder().setUserId(user_id).setUserAddress(user_address).
          setAuctionId(auction_id).setItem(item).setMaxBid(max_bid).
          setRandomAuctionId(random_auction_id).setRandomUserId(random_user_id).
          setRandomItem(random_item).setRandomMaxBid(random_max_bid).build();
        response = blockingStub.notifyNode(inforequest); 
      } catch (RuntimeException e) {
        System.out.println("RPC Error: Failed to establish communication with server");
      }
      return response;
    }

    public Iterator<NodeInfo> findNode(String user_id, String user_address){
      Iterator<NodeInfo> response = null;
      try
      {
        NodeInfo inforequest = NodeInfo.newBuilder().setUserId(user_id).setUserAddress(user_address).build();
        response = blockingStub.findNode(inforequest); 
      } catch (RuntimeException e) {
        System.out.println("RPC Error: Failed to establish communication with server");
      }
      return response;
    }

    public Iterator<NodeInfo> lookupNode(String user_id, String user_address){
      Iterator<NodeInfo> response = null;
      try
      {
        NodeInfo inforequest = NodeInfo.newBuilder().setUserId(user_id).setUserAddress(user_address).build();
        response = blockingStub.lookupNode(inforequest); 
      } catch (RuntimeException e) {
        System.out.println("RPC Error: Failed to establish communication with server");
      }
      return response;
    }  

    public void makeTransaction(String buyer_id, double amount, String seller_id){
      try
      {
        TransactionInfo inforequest = TransactionInfo.newBuilder()
          .setBuyerId(buyer_id)
          .setAmount(amount)
          .setSellerId(seller_id)
          .build();
        NodeResponse response = blockingStub.makeTransaction(inforequest);
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "RPC failed", e);
        return;
      }
    }

    public void infoAuction(String seller_id, String auction_id, String item, double amount, String buyer_id){
      try{
        InfoAuction infoauctionrequest = InfoAuction.newBuilder()
          .setSellerId(seller_id)
          .setAuctionId(auction_id)
          .setItem(item)
          .setAmount(amount)
          .setBuyerId(buyer_id)
          .build();
        NodeResponse response = blockingStub.infoAuction(infoauctionrequest);
      } catch(RuntimeException e){
        logger.log(Level.WARNING, "RPC failed", e);
        return;
      }
    }

    public void makeBid(String buyer_id, String auction_id, double amount){
      try{
        MakeBid bidrequest = MakeBid.newBuilder()
          .setBuyerId(buyer_id)
          .setAuctionId(auction_id)
          .setAmount(amount)
          .build();
        NodeResponse response = blockingStub.makeBid(bidrequest);
      } catch(RuntimeException e){
        logger.log(Level.WARNING, "RCP failed", e);
        return;
      }
      
    }

    public void resultsAuction(String auction_id, String buyer_id, double value){
      try{
        ResultsAuction resutltsauctionrequest = ResultsAuction.newBuilder()
          .setAuctionId(auction_id)
          .setBuyerId(buyer_id)
          .setValue(value)
          .build();
        NodeResponse response = blockingStub.resultsAuction(resutltsauctionrequest);
      } catch(RuntimeException e){
        logger.log(Level.WARNING, "RCP failed", e);
        return;
      }
    }
   
    /*
    public void listFiles(List<String> command_comp){
      if(command_comp.size() != 2)
        logger.info("Error: No arguments should be present for command.");
    }

    public void getFileInfo(List<String> command_comp){
      int n_videos_to_find = command_comp.size() - 2;
      // mesma coisa que o anterior
      if(command_comp.size() == 2)
        logger.info("Error: Please specify the name(s) of the video(s) you want to get information of.");
      else{
        for(int video = 0; video < n_videos_to_find; video++)
        {
          // search video
        }
      }
      
    }
    */
}
  