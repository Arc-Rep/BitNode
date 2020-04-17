package bdcc.kademlia;

import java.net.InetAddress;
import java.net.UnknownHostException;

import bdcc.auction.User;
import bdcc.chain.*;
import bdcc.grpc.NodeInfo;
import bdcc.grpc.NodeOperationsClient;

public class NodeActions {
    public static void pingNode(KeyNode node, int server_port,KBucket userBucket, User current_user){
        NodeOperationsClient initial_requester = new NodeOperationsClient(node.getValue(), server_port);
        try
        {
            NodeInfo response = initial_requester.notifyNode(current_user.getUserId(), InetAddress.getLocalHost().getHostAddress());
            System.out.println("User " + Crypto.toHex(response.getUserId())  + " found with address " + response.getUserAddress());
            userBucket.addNode(response.getUserId(), response.getUserAddress());
        }
        catch(UnknownHostException e){
            //if node not found it is removed from the KBucket
            userBucket.removeNode(node);
        }
    }
}