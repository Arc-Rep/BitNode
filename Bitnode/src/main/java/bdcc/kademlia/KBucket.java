package bdcc.kademlia;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;

public class KBucket{
    LinkedList<CopyOnWriteArrayList<KeyNode>> kbucket = new LinkedList<CopyOnWriteArrayList<KeyNode>>();
    int k = 20, B, alpha = 3,node_number = 0, populated_rows = 0;
    String node_id;


    public KBucket(String this_node_id, int bit_size){
        node_id = this_node_id;
        B = bit_size;

        for(int i=1; i <= B; i++)
            kbucket.add(new CopyOnWriteArrayList<KeyNode>());
        
    }

    public synchronized void addNode(String key, String value, byte[] pub_key){
        KeyNode node = new KeyNode(key,value, pub_key);
        double distance = node.compareKeyNodeID(node_id),i;

        for(i=1; Math.pow(2,i) < distance; i++);
        CopyOnWriteArrayList<KeyNode> correct_list = kbucket.get((int) i);
        addNodeToCorrectList(node, correct_list);
        
    }

    public synchronized void addNodeToCorrectList(KeyNode newNode, CopyOnWriteArrayList<KeyNode> list){
        for(KeyNode node: list)
        {
            if(node.Key == newNode.Key)
            {
                list.remove(node);
                list.add(0, newNode);
                return;
            }
        }

        list.add(0, newNode);
        if(list.size() > k)
        {
            list.remove(k);
            this.node_number--;
        }
        else if(list.size() == 1)
        {
            this.populated_rows++;
            this.node_number++;
        }
        else this.node_number++;   
    }

    public synchronized void removeNode(KeyNode newNode){
        double distance = newNode.compareKeyNodeID(node_id),i;

        for(i=1; Math.pow(2,i) < distance; i++);
        CopyOnWriteArrayList<KeyNode> correct_list = kbucket.get((int) i);

        if(correct_list.size() == 0) return;

        for(KeyNode node: correct_list)
        {
            if(node.Key == newNode.Key)
            {
                correct_list.remove(node);
                this.node_number--;
                if(correct_list.size() == 0)
                    this.populated_rows--;
                return;
            }
        }
        return;
    }

    public synchronized LinkedList<KeyNode> searchNodeList(String search_node_id, CopyOnWriteArrayList<KeyNode> list){
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

    public synchronized int getListIndex(LinkedList<Double> list, double check){
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

    public synchronized LinkedList<KeyNode> searchNodeKBucket(String search_node_id){
        CopyOnWriteArrayList<KeyNode> best_list = null;
        double best_compare = -1, temp;

        for(CopyOnWriteArrayList<KeyNode> sublist: kbucket){
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

    public synchronized LinkedList<KeyNode> findNearestNodes(String search_node_id, boolean by_alpha){
        LinkedList<KeyNode> nodeslist = new LinkedList<KeyNode>(), ordered_list = null;
        CopyOnWriteArrayList<KeyNode> best_list = null;
        double best_compare = -1, temp, has_prev = 0, prev_min = 0, limit;
        if(by_alpha) limit = alpha;
        else limit = k;
        while(nodeslist.size() < limit && nodeslist.size() < node_number)
        {
            best_list = null;
            for(CopyOnWriteArrayList<KeyNode> sublist: kbucket){
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
            ordered_list = findNearestNodeList(search_node_id, best_list);
            for(int i=0 ; nodeslist.size() < limit && i < ordered_list.size() ; i++)
            {
                nodeslist.add(ordered_list.get(i));
            }
        }

        return nodeslist;
    }

    public synchronized LinkedList<KeyNode> findNearestNodeList(String search_node_id, CopyOnWriteArrayList<KeyNode> correct_list){
        int i;
        LinkedList<KeyNode> orderedList = new LinkedList<KeyNode>();
        for(KeyNode node: correct_list)
        {
            for(i=0 ; i < orderedList.size() ; i++)
                if(node.compareKeyNodeID(search_node_id) <= orderedList.get(i).compareKeyNodeID(search_node_id))
                    break;
            orderedList.add(i, node);
        }
        return orderedList;
    }

    public synchronized LinkedList<KeyNode> lookupNode(String search_node_id) {
        return findNearestNodes(search_node_id, false);
    }

    public synchronized LinkedList<KeyNode> findNode(String search_node_id) {
        return findNearestNodes(search_node_id, true);
    }

    public synchronized KeyNode getRandomNode(){
        if(node_number == 0) return null;
        Random r = new Random();
        int row_num = ((r.nextInt() % B) % populated_rows), column_num = r.nextInt() % k, current = 0;
        CopyOnWriteArrayList<KeyNode> selected_list = null;
        KeyNode to_return = null;
        for(CopyOnWriteArrayList<KeyNode> sublist: kbucket)     //choose random row
        {
            if(sublist.size() > 0)
            {
                if(current == row_num)
                {
                    selected_list = sublist;
                    break;
                }
                else current++;
            }
        }

        if(selected_list == null || selected_list.size() == 0) return null;

        current = 0;
        column_num = column_num % selected_list.size();

        for(KeyNode subnode: selected_list)
        {
            if(current == column_num)
            {
                to_return = subnode;
                break;
            }
            else current++;
        }
        return to_return;
    }
    
    public String getUserId(){
        return node_id;
    }

    public int getAlpha(){
        return alpha;
    }

}