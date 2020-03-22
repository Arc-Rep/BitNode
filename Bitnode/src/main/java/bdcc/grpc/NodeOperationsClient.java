package bdcc.grpc;

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


public class NodeOperationsClient {
    private static final Logger logger = Logger.getLogger(NodeOperationsClient.class.getName());
  
    private final ManagedChannel channel;
    private final NodeOperationsGrpc.NodeOperationsBlockingStub blockingStub;
  
    /** Construct client connecting to GrpcFlow server at {@code host:port}. */
    public NodeOperationsClient(String host, int port) {
      channel = ManagedChannelBuilder.forAddress(host, port)
          .usePlaintext()
          .build();
      blockingStub = NodeOperationsGrpc.newBlockingStub(channel);
    }
  
    public void shutdown() throws InterruptedException {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /*public static void chooseGrpcRequest(GrpcNodeClient client, Command command, List<String> command_comp){
      switch(command){
        case DOWNLOAD_FILE: client.downloadFile(command_comp);
                            break;
        case LIST_FILE: client.listFiles(command_comp);
                        break;
        case INFO_FILE: client.getFileInfo(command_comp);
                        break;
        default: logger.info("Error: Command went to the wrong place...");
      }
    }*/

    public void notifyNode(String user_id, String user_address){
      try
      {
        NodeNotification inforequest = NodeNotification.newBuilder().setUserId(user_id).setUserAddress(user_address).build();
        NodeInfo response = blockingStub.notifyNode(inforequest);
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "RPC failed", e);
        return;
      }
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

    public static void main(String[] args) throws Exception {
      NodeOperationsClient client = new NodeOperationsClient("localhost", 50051);
      BufferedReader reader =  
        new BufferedReader(new InputStreamReader(System.in));
      /*CommandManager command_manager = new CommandManager(logger);
      Command chosen_command;*/
      List<String> command_comp;
      String command_string = "";

      logger.info("BitNode successfully initialized.");

      try {/*
        while((command_string = reader.readLine()) != "exit") {
          if(command_string != ""){
            command_comp = FieldSegmentation.Segment(command_string);
            chosen_command = command_manager.deduceCommand(command_comp);
            if((chosen_command.getCommandIndex() & Command.GRPC_COMMAND.getCommandIndex()) != 0)      // if command is grpc request
              chooseGrpcRequest(client, chosen_command, command_comp);
          }
        }    */
      } finally {
        logger.info("Exiting...");
        client.shutdown();
      }
    }

  }
  