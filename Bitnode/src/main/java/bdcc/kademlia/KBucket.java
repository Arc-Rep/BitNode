package bdcc.kademlia;

import java.util.LinkedList;

public class KBucket{
    LinkedList<LinkedList<KeyNode>> kbucket = new LinkedList<LinkedList<KeyNode>>();
    int k = 20, B, alpha = 3,node_number = 0;
    String node_id;


    public KBucket(String this_node_id, int bit_size){
        node_id = this_node_id;
        B = bit_size;

        for(int i=1; i <= B; i++)
            kbucket.add(new LinkedList<KeyNode>());
        
    }

    public void addNode(String key, String value){
        KeyNode node = new KeyNode(key,value);
        double distance = node.compareKeyNodeID(node_id),i;

        for(i=1; Math.pow(2,i) < distance; i++);
        LinkedList<KeyNode> correct_list = kbucket.get((int) i);
        correct_list.push(node);
        if(correct_list.size() > k) correct_list.remove(k);
        else this.node_number++;   
    }

    public LinkedList<KeyNode> searchNodeList(String search_node_id, LinkedList<KeyNode> list){
        LinkedList<Double> best_values = new LinkedList<Double>();
        LinkedList<KeyNode> best_nodes = new LinkedList<KeyNode>();

        for(int i=0; i<list.size();i++)
        {
            int index = getListIndex(best_values,list.get(i).compareKeyNodeID(search_node_id));
            if(index != alpha) 
            {
                best_nodes.add(list.get(index));
                if(best_nodes.size() > alpha) best_nodes.remove(alpha);
            }
            
        }
        return best_nodes;
    }

    public int getListIndex(LinkedList<Double> list, double check){
        int i;
        for(i = 0; i < list.size() && i < alpha; i++){
            if(check > list.get(i)) break;
        }
        if(i != alpha)
        {
            list.add(check);
            if(list.size() > alpha) list.remove(alpha);
        }
        return i;
    }

    public LinkedList<KeyNode> searchNodeKBucket(String search_node_id){
        LinkedList<KeyNode> nodeslist = new LinkedList<KeyNode>(), best_list = null;
        double best_compare = -1, temp;

        for(LinkedList<KeyNode> sublist: kbucket){
            if(best_list == null && sublist.size() > 0)
            {
                best_list = sublist;
                best_compare = sublist.get(0).compareKeyNodeID(search_node_id);

            } 
            else if(best_list != null && sublist.size() > 0)
            {
                if( (temp = sublist.get(0).compareKeyNodeID(search_node_id)) < best_compare)
                {
                    best_list = sublist;
                    best_compare = temp;
                }
            }
        }

        if(best_list == null) return null;

        return searchNodeList(search_node_id, best_list);
    }

    public LinkedList<KeyNode> findNodeInitialization(String search_node_id){
        LinkedList<KeyNode> nodeslist = new LinkedList<KeyNode>(), best_list = null;
        double best_compare = -1, temp, has_prev = 0, prev_min = 0;
        while(nodeslist.size() < k && nodeslist.size() < node_number)
        {
            best_list = null;
            for(LinkedList<KeyNode> sublist: kbucket){
                if(best_list == null && sublist.size() > 0)
                {
                    temp = sublist.get(0).compareKeyNodeID(search_node_id);
                    if(has_prev == 0 || temp > prev_min)
                    best_list = sublist;
                    best_compare = sublist.get(0).compareKeyNodeID(search_node_id);

                } 
                else if(best_list != null && sublist.size() > 0)
                {
                    temp = sublist.get(0).compareKeyNodeID(search_node_id);
                    if(sublist.get(0).compareKeyNodeID(search_node_id) < best_compare && (has_prev == 0 || temp > prev_min))
                    {
                        best_list = sublist;
                        best_compare = temp;
                    }
                }
            }
            if(best_list == null) break;
            prev_min = best_list.get(0).compareKeyNodeID(search_node_id);
            has_prev = 1;
            for(int i=0;nodeslist.size() < k && i<best_list.size();i++)
            {
                nodeslist.add(best_list.get(i));
            }
        }

        return nodeslist;
    }

    
    public String getUserId(){
        return node_id;
    }



}