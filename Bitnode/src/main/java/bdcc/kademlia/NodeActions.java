package bdcc.kademlia;

import java.net.InetAddress;

import bdcc.auction.User;
import bdcc.chain.*;
import bdcc.grpc.NodeInfo;
import bdcc.grpc.NodeOperationsClient;

public class NodeActions {
    public static void pingNode(String node_address, int server_port,KBucket userBucket, User current_user){
        NodeOperationsClient initial_requester = new NodeOperationsClient(node_address, server_port);
        try
        {
            NodeInfo response = initial_requester.notifyNode(current_user.getUserId(), InetAddress.getLocalHost().getHostAddress());
            System.out.println("User " + Crypto.toHex(response.getUserId())  + " found with address " + response.getUserAddress());
            userBucket.addNode(response.getUserId(), response.getUserAddress());
        }
        catch(Exception e){
            System.out.println("Error: Server not found.");
        }
    }
}