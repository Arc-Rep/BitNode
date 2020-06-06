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
    private KBucket userBucket;
    private User user;
    private AuctionList auction_list;
    private String server_address;
    private String server_id;
    private TransactionManager transaction_manager;
    private NodeBlockChain block_chain;

    public NodeOperationsServer(int port, String user_id, String address, 
                                KBucket user_bucket, User this_user, AuctionList list, 
                                TransactionManager t_manager, NodeBlockChain chain) throws Exception {
      this.server_id = user_id;
      this.server_address = address;
      this.userBucket = user_bucket;
      this.auction_list = list;
      this.user = this_user;
      this.transaction_manager = t_manager;
      this.block_chain = chain;

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
      public void registerNode(NodeAddress node_address, StreamObserver<NodeInfo> responseObserver){
        String address = node_address.getNodeAddress(), node_id = "";

        if(address != ""){
          node_id = Crypto.hashString(user.getPubKey().toString() + address + user.getPrivateKey().toString());
        }

        NodeInfo reply = NodeInfo.newBuilder().setUserId(node_id).setUserAddress(address).build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      }
      
      @Override
      public void notifyNode(NodeNotification notification, StreamObserver<NodeNotification> responseObserver) {
        Auction random_auction = auction_list.getRandomAuction(), user_auction = user.getUserAuction();
        NodeActions.proccessPingNode(notification, userBucket, user, auction_list);
        byte[] node_public_key = Crypto.convertStringToBytes(notification.getPublicKey());
        String auction_buyer = "", random_auction_buyer = "";

        try {

          if(user_auction != null)
            if(user_auction.getHighestBidder() != "")
              auction_buyer = Crypto.doFullStringEncryption(user_auction.getHighestBidder(), node_public_key);

          if(random_auction != null)
            if(random_auction.getHighestBidder() != "")
              random_auction_buyer = Crypto.doFullStringEncryption(random_auction.getHighestBidder(), node_public_key);

          NodeNotification reply = NodeNotification.newBuilder()
                              .setUserId(userBucket.getUserId()).setUserAddress(server_address).
                              setPublicKey(Crypto.convertBytesToString(user.getPubKey())).
                              setAuctionId((user_auction == null) ? "" : Crypto.doFullStringEncryption(user_auction.getAuctionId(), node_public_key)).
                              setItem((user_auction == null) ? "" : Crypto.doFullStringEncryption(user_auction.getItem(), node_public_key)).
                              setMaxBid((user_auction == null) ? "" : Crypto.doFullDoubleEncryption(user_auction.getCurrentHighestAmount(), node_public_key)).
                              setRandomAuctionId((random_auction == null) ? "" : Crypto.doFullStringEncryption(random_auction.getAuctionId(), node_public_key)).
                              setRandomUserId((random_auction == null) ? "" : Crypto.doFullStringEncryption(random_auction.getSeller(), node_public_key)).
                              setRandomItem((random_auction == null) ? "" : Crypto.doFullStringEncryption(random_auction.getItem(), node_public_key)).
                              setRandomMaxBid((random_auction == null) ? "" : Crypto.doFullDoubleEncryption(random_auction.getCurrentHighestAmount(), node_public_key)).
                              setAuctionBuyer(auction_buyer).setRandomAuctionBuyer(random_auction_buyer).
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
        String payer_id = "", receiver_id = "";
        Double amount = -1.0;
        try{
          payer_id =  Crypto.doFullStringDecryption(transactionInfo.getBuyerId(), user.getPrivateKey());
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
          NodeActions.registerTransaction(new Transaction(payer_id, receiver_id, amount), userBucket, user, 4444);
        
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
        Boolean success = false;
        String seller_id = null;
        Double amount;
        try {
          String seller_address = resultsAuction.getSellerAddress(),
          buyer_id = Crypto.doFullStringDecryption(resultsAuction.getBuyerId(), user.getPrivateKey()),
          auction_id = Crypto.doFullStringDecryption(resultsAuction.getAuctionId(), user.getPrivateKey());
          amount = Crypto.doFullDoubleDecryption(resultsAuction.getValue(), user.getPrivateKey());
          byte[] seller_public_key = Crypto.convertStringToBytes(resultsAuction.getSellerPublicKey());
          seller_id = resultsAuction.getSellerId();

          Auction saved_auction = auction_list.getAuctionById(auction_id);

          System.out.println("Amount " + amount);
          System.out.println("Wallet " + user.getWallet() + " and " + user.getVaulted());

          if(saved_auction == null)
            System.out.println("Received result of unknown auction");
          else if(!(saved_auction.getHighestBidder().equals(buyer_id) && buyer_id.equals(user.getUserId()))){
            System.out.println("Received auction has mistaken User ID");
          }
          else if(!saved_auction.getSeller().equals(seller_id) || (saved_auction.getCurrentHighestAmount() != amount))
            System.out.println("Participating auction has errors discrepancies in data. Possible fraud. Cancelling auction...");
          else{
            success = true;
            System.out.println("Auction " + auction_id + " has ended! You are the winner with a bid of " + 
              Double.toString(amount) +"! Awaiting transaction confirmation from server.");
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

          if(success){
            Transaction transaction = new Transaction(user.getUserId(),seller_id,amount);
            if(!user.getUserId().equals("Server"))
              NodeActions.registerTransaction(transaction, userBucket, user, 4444);
            else
              transaction_manager.addOrCheckTransaction(transaction, user.getUserId());
          }
        } catch (Exception e) {
          System.out.println("Auction results server error: " + e.getMessage());
        }
      }

      @Override
      public void notifyTransaction(TransactionSubmission transaction, StreamObserver<NodeResponse> responseObserver){
        Boolean success = false;
        NodeResponse response;
        Transaction to_register = null;
        String origin_id = "";
        try{
          String buyer_id = Crypto.doFullStringDecryption(transaction.getBuyerId(), user.getPrivateKey()),
          seller_id = Crypto.doFullStringDecryption(transaction.getSellerId(), user.getPrivateKey());
          origin_id = Crypto.doFullStringDecryption(transaction.getOriginId(), user.getPrivateKey());
          Double amount = Crypto.doFullDoubleDecryption(transaction.getAmount(), user.getPrivateKey());

          
          if((!user.getUserId().equals("Server")) && (origin_id.equals(buyer_id) || origin_id.equals(seller_id)))
            response = NodeResponse.newBuilder().setStatus("Rejected").build();
          else
          {
            to_register = new Transaction(buyer_id, seller_id, amount);
            response = NodeResponse.newBuilder().setStatus("Ok").build();
            success = true;
          }

        } catch (Exception e){
          response = NodeResponse.newBuilder().setStatus("Rejected").build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        if(success)
        {
          Transaction verified_transaction = transaction_manager.addOrCheckTransaction(to_register, origin_id);
          
          if(verified_transaction != null){
            block_chain.addTransaction(verified_transaction);
            NodeActions.spreadTransactionAccrossNetwork(verified_transaction, userBucket);
          }
  
        }
      }

      @Override
      public void addTransactionToBlockChain(TransactionInfo transaction, StreamObserver<NodeResponse> responseObserver){
        NodeResponse response = null;

        try{
          String buyer_id = Crypto.doFullStringDecryption(transaction.getBuyerId(), user.getPrivateKey()),
          seller_id = Crypto.doFullStringDecryption(transaction.getSellerId(), user.getPrivateKey());
          Double amount = Crypto.doFullDoubleDecryption(transaction.getAmount(), user.getPrivateKey());

          Transaction to_register = new Transaction(buyer_id, seller_id, amount);

          block_chain.addTransaction(to_register);
          if(buyer_id.equals(user.getUserId())){ 
            System.out.println("Successfully transferred " + amount + " to user " + Crypto.toHex(seller_id));
            user.withdrawAmount(amount);
          }
          else if(seller_id.equals(user.getUserId())){ 
            System.out.println("Successfully received " + amount + " from user " + Crypto.toHex(buyer_id));
            user.receiveMoneyTransfer(amount);
          }

          response = NodeResponse.newBuilder().setStatus("Ok").build();
        } catch (Exception e) {
          response = NodeResponse.newBuilder().setStatus("ERROR").build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
    }

}
  