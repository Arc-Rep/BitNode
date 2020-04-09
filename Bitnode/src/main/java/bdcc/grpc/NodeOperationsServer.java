package bdcc.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.LinkedList;

import bdcc.kademlia.*;

public class NodeOperationsServer {
    private static final Logger logger = Logger.getLogger(NodeOperationsServer.class.getName());
  
    /* The port on which the server should run */

    private Server server;
    public static KBucket userBucket;
    private String server_address;
    private String server_id;

    public NodeOperationsServer(int port, String user_id, String address, KBucket user_bucket) throws Exception {
      this.server_id = user_id;
      this.server_address = address;
      this.userBucket = user_bucket;
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

    private class NodeOperationsService extends NodeOperationsGrpc.NodeOperationsImplBase {
      
      
      @Override
      public void notifyNode(NodeInfo req, StreamObserver<NodeInfo> responseObserver) {
        //make operations to hashtable
        String[] replyContent = {"Received with success","Yep, totally"};
        NodeInfo reply = NodeInfo.newBuilder()
                            .setUserId(userBucket.getUserId())
                            .setUserAddress(server_address)
                            .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      }

      @Override
      public void findNode(NodeInfo node_id, StreamObserver<NodeInfo> responseObserver) {
        //make operations to hashtable
        LinkedList<KeyNode> node_list = userBucket.searchNodeKBucket(node_id.getUserId());
        NodeInfo reply =                      // send current node
          NodeInfo.newBuilder()
            .setUserId(server_id)
            .setUserAddress(server_address)
            .build();
        responseObserver.onNext(reply);
        if(node_list != null)           // if there are no other nodes
        {
          for(KeyNode node: node_list)
          {
            reply = 
              NodeInfo.newBuilder()
                .setUserId(node.getKey())
                .setUserAddress(node.getValue())
                .build();
            responseObserver.onNext(reply);
          }
        }
        responseObserver.onCompleted();
        userBucket.addNode(node_id.getUserId(), node_id.getUserAddress());
      }

      @Override
      public void makeTransaction(TransactionInfo transactionInfo, StreamObserver<NodeResponse> responseObserver) {
        NodeResponse.Builder replyBuilder = NodeResponse.newBuilder().setStatus("Ok");

        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
      }

    }
  }
  