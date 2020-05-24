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
        byte[] node_public_key =Crypto.convertStringToBytes(notification.getPublicKey());
        try {
          NodeNotification reply = NodeNotification.newBuilder()
                              .setUserId(userBucket.getUserId()).setUserAddress(server_address).
                              setPublicKey(Crypto.convertBytesToString(user.getPubKey())).
                              setAuctionId((user_auction == null) ? "" : Crypto.convertBytesToString(Crypto.encrypt(node_public_key,Crypto.convertStringToBytes(user_auction.getAuctionId())))).
                              setItem((user_auction == null) ? "" : Crypto.convertBytesToString(Crypto.encrypt(node_public_key, Crypto.convertStringToBytes(user_auction.getItem())))).
                              setMaxBid((user_auction == null) ? 0 : user_auction.getValue()).
                              setRandomAuctionId((random_auction == null) ? "" : Crypto.convertBytesToString(Crypto.encrypt(node_public_key, Crypto.convertStringToBytes(random_auction.getAuctionId())))).
                              setRandomUserId((random_auction == null) ? "" : Crypto.convertBytesToString(Crypto.encrypt(node_public_key, Crypto.convertStringToBytes(random_auction.getSeller())))).
                              setRandomItem((random_auction == null) ? "" : Crypto.convertBytesToString(Crypto.encrypt(node_public_key, Crypto.convertStringToBytes(random_auction.getItem())))).
                              setRandomMaxBid((random_auction == null) ? 0 : random_auction.getValue()).build();
          responseObserver.onNext(reply);
        } catch(Exception e) {
          System.out.println(e.getMessage());
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
        System.out.println("Received");
        System.out.println(node_id.getPublicKey()); 
        System.out.println("Sending"); 
        System.out.println(Crypto.convertBytesToString(user.getPubKey()));
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
        NodeResponse.Builder replyBuilder = NodeResponse.newBuilder().setStatus("Ok");

        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
      }

      @Override
      public void infoAuction(InfoAuction infoAuction, StreamObserver<NodeResponse> responseObserver){
        Auction temp;
        if(infoAuction.getBuyerId() == ""){
          temp = new Auction(infoAuction.getSellerId(), infoAuction.getItem(), infoAuction.getAmount(), infoAuction.getAuctionId(), null);
        }
        else{
          Bid bid_info = new Bid(infoAuction.getAuctionId(), infoAuction.getBuyerBid(), infoAuction.getBuyerId());
          temp = new Auction(infoAuction.getSellerId(), infoAuction.getItem(), infoAuction.getAmount(), infoAuction.getAuctionId(), bid_info);
        }
        //add the announced aution to the list
        auction_list.addToAuctionList(temp);

        NodeResponse.Builder replyBuilder = NodeResponse.newBuilder().setStatus("Ok");

        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
      }

      @Override
      public void makeBid(MakeBid makeBid, StreamObserver<NodeResponse> responseObserver){
        System.out.println("Auction " + makeBid.getAuctionId() + " has a new highest bidder, " + makeBid.getBuyerId() + " with " + makeBid.getAmount());
        NodeResponse.Builder replyBuilder = NodeResponse.newBuilder().setStatus("Ok");

        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
      }

      @Override
      public void resultsAuction(ResultsAuction resultsAuction, StreamObserver<NodeResponse> responseObserver){
        System.out.println("Auction " + resultsAuction.getAuctionId() + " has ended! The winner is " + resultsAuction.getBuyerId() + " with a bid of " + resultsAuction.getValue() +"!");
        NodeResponse.Builder replyBuilder = NodeResponse.newBuilder().setStatus("Ok");

        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
      }

    }
  }
  