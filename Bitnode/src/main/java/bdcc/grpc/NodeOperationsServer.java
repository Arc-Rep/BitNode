package bdcc.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;
import java.util.LinkedList;

import bdcc.kademlia.*;
import bdcc.auction.Auction;
import bdcc.auction.AuctionList;
import bdcc.auction.Bid;
import bdcc.auction.User;
import bdcc.chain.*;

public class NodeOperationsServer {
    private static final Logger logger = Logger.getLogger(NodeOperationsServer.class.getName());
  
    /* The port on which the server should run */

    private Server server;
    private static KBucket userBucket;
    private static User user;
    private static AuctionList auction_list;
    private String server_address;
    private String server_id;

    public NodeOperationsServer(int port, String user_id, String address, 
                                KBucket user_bucket, User this_user, AuctionList list) throws Exception {
      this.server_id = user_id;
      this.server_address = address;
      this.userBucket = user_bucket;
      this.auction_list = list;
      this.user = this_user;
      server = ServerBuilder.forPort(port)
          .addService(new NodeOperationsService())
          .build()
          .start();
      logger.info("Server started, listening on " + port);
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          // Use stderr here since the logger may have been reset by its JVM shutdown hook.
          System.err.println("*** shutting down gRPC server since JVM is shutting down");
          NodeOperationsServer.this.stop();
          System.err.println("*** server shut down");
        }
      });
    }
  
    public void stop() {
      if (server != null) {
        server.shutdown();
      }
    }
  
    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
      if (server != null) {
        server.awaitTermination();
      }
    }

    public void doShutdown() throws InterruptedException {
      if (server != null) {
        server.shutdown();
      }
    }

    private class NodeOperationsService extends NodeOperationsGrpc.NodeOperationsImplBase {
      
      
      @Override
      public void notifyNode(NodeNotification notification, StreamObserver<NodeNotification> responseObserver) {
        Auction random_auction = auction_list.getRandomAuction(), user_auction = user.getUserAuction();
        NodeActions.proccessPingNode(notification, userBucket, user, auction_list);
        byte[] node_public_key = Crypto.convertStringToBytes(notification.getPublicKey());
        try {
          NodeNotification reply = NodeNotification.newBuilder()
                              .setUserId(userBucket.getUserId()).setUserAddress(server_address).
                              setPublicKey(Crypto.convertBytesToString(user.getPubKey())).
                              setAuctionId((user_auction == null) ? "" : Crypto.doFullStringEncryption(user_auction.getAuctionId(), node_public_key)).
                              setItem((user_auction == null) ? "" : Crypto.doFullStringEncryption(user_auction.getItem(), node_public_key)).
                              setMaxBid((user_auction == null) ? "" : Crypto.doFullDoubleEncryption(user_auction.getValue(), node_public_key)).
                              setRandomAuctionId((random_auction == null) ? "" : Crypto.doFullStringEncryption(random_auction.getAuctionId(), node_public_key)).
                              setRandomUserId((random_auction == null) ? "" : Crypto.doFullStringEncryption(random_auction.getSeller(), node_public_key)).
                              setRandomItem((random_auction == null) ? "" : Crypto.doFullStringEncryption(random_auction.getItem(), node_public_key)).
                              setRandomMaxBid((random_auction == null) ? "" : Crypto.doFullDoubleEncryption(random_auction.getValue(), node_public_key)).
                              build();
          responseObserver.onNext(reply);
        } catch(Exception e) {
          System.out.println("Node notification server error: " + e.getMessage());
        }
        responseObserver.onCompleted();
        
      }

      @Override
      public void findNode(NodeSecInfo node_id, StreamObserver<NodeSecInfo> responseObserver) {

        LinkedList<KeyNode> node_list = userBucket.findNode(node_id.getUserId());
        NodeSecInfo reply =                      // send current node
          NodeSecInfo.newBuilder()
            .setUserId(server_id)
            .setUserAddress(server_address)
            .setPublicKey(Crypto.convertBytesToString(user.getPubKey()))
            .build();
        responseObserver.onNext(reply);
        if(node_list != null)           // if there are no other nodes
        {
          for(KeyNode node: node_list)
          {
            reply = 
              NodeSecInfo.newBuilder()
                .setUserId(node.getKey())
                .setUserAddress(node.getValue())
                .setPublicKey(Crypto.convertBytesToString(node.getPubKey()))
                .build();
            responseObserver.onNext(reply);
          }
        }
        responseObserver.onCompleted();
        userBucket.addNode(node_id.getUserId(), node_id.getUserAddress(), 
                            Crypto.convertStringToBytes(node_id.getPublicKey()));
      }

      @Override
      public void lookupNode(NodeSecInfo node_id, StreamObserver<NodeSecInfo> responseObserver) {

        LinkedList<KeyNode> node_list = userBucket.lookupNode(node_id.getUserId());

        NodeSecInfo reply =                      // send current node
          NodeSecInfo.newBuilder()
            .setUserId(server_id)
            .setUserAddress(server_address)
            .setPublicKey(Crypto.convertBytesToString(user.getPubKey()))
            .build();
        responseObserver.onNext(reply);
        //System.out.println("User " + Crypto.toHex(node_id.getUserId()) + " from " + node_id.getUserAddress() + " connected");
        if(node_list != null)           // if there are no other nodes
        {
          for(KeyNode node: node_list)
          {
            reply = 
              NodeSecInfo.newBuilder()
                .setUserId(node.getKey())
                .setUserAddress(node.getValue())
                .setPublicKey(Crypto.convertBytesToString(node.getPubKey()))
                .build();
            responseObserver.onNext(reply);
          }
        }
        responseObserver.onCompleted();
        userBucket.addNode(node_id.getUserId(), node_id.getUserAddress(),
                            Crypto.convertStringToBytes(node_id.getPublicKey()));
      }

      @Override
      public void makeTransaction(TransactionInfo transactionInfo, StreamObserver<NodeResponse> responseObserver) {
        Boolean success = false;
        NodeResponse.Builder replyBuilder;
        Double amount = -1.0;
        try{
          String payer_id =  Crypto.doFullStringDecryption(transactionInfo.getBuyerId(), user.getPrivateKey()),
          receiver_id = Crypto.doFullStringDecryption(transactionInfo.getSellerId(), user.getPrivateKey());
          amount = Crypto.doFullDoubleDecryption(transactionInfo.getAmount(), user.getPrivateKey());
          if(receiver_id.equals(user.getUserId())){
            replyBuilder = NodeResponse.newBuilder().setStatus("Ok");
            success = true;
            System.out.println("Received transaction from " + Crypto.toHex(payer_id) + " with value " + amount);
          }
          else replyBuilder = NodeResponse.newBuilder().setStatus("Denied");
        } catch (Exception e) {
          System.out.println(e.getMessage());
          replyBuilder = NodeResponse.newBuilder().setStatus("Denied");
        }
        


        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();

        if(success) 
        {
          //por em blockchain
          user.receiveMoneyTransfer(amount);
        }
      }

      @Override
      public void infoAuction(NodeSecInfo buyer_info, StreamObserver<InfoAuction> responseObserver){
        Auction user_auction = user.getUserAuction();
        byte[] node_public_key = Crypto.convertStringToBytes(buyer_info.getPublicKey());
        try {
          InfoAuction.Builder replyBuilder = InfoAuction.newBuilder().
          setSellerId((user_auction == null) ? "" : Crypto.doFullStringEncryption(user.getUserId(), node_public_key)).
          setAuctionId((user_auction == null) ? "" : Crypto.doFullStringEncryption(user_auction.getAuctionId(), node_public_key)). 
          setAmount((user_auction == null) ? "" : Crypto.doFullDoubleEncryption(user_auction.getValue(), node_public_key)).
          setItem((user_auction == null) ? "" : Crypto.doFullStringEncryption(user_auction.getItem(), node_public_key)). 
          setBuyerId((user_auction == null) ? "" : Crypto.doFullStringEncryption(user_auction.getHighestBidder(), node_public_key)). 
          setBuyerBid((user_auction == null) ? "" : Crypto.doFullDoubleEncryption(user_auction.getHighestBid(), node_public_key));
          responseObserver.onNext(replyBuilder.build());
        }
        catch(Exception e){
          System.out.println("InfoAuction server error: " + e.getMessage());
        }
        responseObserver.onCompleted();
        userBucket.addNode(buyer_info.getUserId(), buyer_info.getUserAddress(), node_public_key);
        
      }

      @Override
      public void makeBid(MakeBid makeBid, StreamObserver<NodeResponse> responseObserver){
        String status;
        try{
          String auction_id = Crypto.doFullStringDecryption(makeBid.getAuctionId(), user.getPrivateKey()),
          node_id = Crypto.doFullStringDecryption(makeBid.getBuyerId(), user.getPrivateKey());
          Double amount = Crypto.doFullDoubleDecryption(makeBid.getAmount(), user.getPrivateKey());
          Bid received_bid = new Bid(auction_id, amount, node_id);
          if(!user.checkActiveAuction(auction_id)) status = "ENDED";
          else if(!user.processBid(received_bid)) status = "REFUSED";
          else status = "OK";
          if(status.equals("OK")) System.out.println("Accepted bid from " + Crypto.toHex(node_id) + " with value " + amount);
        } catch(Exception e){
          status = "REFUSED";
        }
        
        NodeResponse.Builder replyBuilder = NodeResponse.newBuilder().setStatus(status);
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
      }

      @Override
      public void resultsAuction(ResultsAuction resultsAuction, StreamObserver<TransactionInfo> responseObserver){
        try {
          String seller_id = resultsAuction.getSellerId(), seller_address = resultsAuction.getSellerAddress(),
          buyer_id = Crypto.doFullStringDecryption(resultsAuction.getBuyerId(), user.getPrivateKey()),
          auction_id = Crypto.doFullStringDecryption(resultsAuction.getAuctionId(), user.getPrivateKey());
          Double amount = Crypto.doFullDoubleDecryption(resultsAuction.getValue(), user.getPrivateKey());
          byte[] seller_public_key = Crypto.convertStringToBytes(resultsAuction.getSellerPublicKey());
          Boolean success = false;

          Auction saved_auction = auction_list.getAuctionById(auction_id);

          if(saved_auction == null)
            System.out.println("Received result of unknown auction");
          else if(!(saved_auction.getHighestBidder().equals(buyer_id) && buyer_id.equals(user.getUserId()))){
            System.out.println("Received auction has mistaken User ID");
            System.out.println(buyer_id + "\n\n\n\n");
            System.out.println(saved_auction.getHighestBidder() + "\n\n\n\n");
            System.out.println(user.getUserId() + "\n\n\n\n");
          }
          else if(!saved_auction.getSeller().equals(seller_id) || (saved_auction.getValue() != amount))
            System.out.println("Participating auction has errors discrepancies in data. Possible fraud. Cancelling auction...");
          else if(!user.withdrawAmount(amount))
            System.out.println("Auction cancelled. For some reason you have no money");
          else{
            success = true;
            System.out.println("Auction " + auction_id + " has ended! You are the winner with a bid of " + 
              Double.toString(amount) +"!");
          }
          try{
            TransactionInfo.Builder replyBuilder = TransactionInfo.newBuilder()
              .setBuyerId(success ? Crypto.doFullStringEncryption(buyer_id, seller_public_key) : "")
              .setAmount(success ? Crypto.doFullDoubleEncryption(amount, seller_public_key) : "0")
              .setSellerId(success ? Crypto.doFullStringEncryption(seller_id, seller_public_key) : "");

            responseObserver.onNext(replyBuilder.build());
          }
          catch(Exception e){
            System.out.println("Auction results server error: " + e.getMessage());
          }
          responseObserver.onCompleted();
          userBucket.addNode(seller_id, seller_address, seller_public_key);
          auction_list.updateList(auction_id, saved_auction, 2);
        } catch (Exception e) {
          System.out.println("Auction results server error: " + e.getMessage());
        }
      }
    }
  }
  