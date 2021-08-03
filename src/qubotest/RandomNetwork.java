/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qubotest;

/**
 *
 * @author micha
 */
public class RandomNetwork extends Network 
{
    public RandomNetwork(int numNodes, int numLinks, int T, int num_scenarios)
    {
        super(T, num_scenarios);
        Node.next_id = 1;
        
        
        for(int i = 0; i < numNodes; i++)
        {
            nodes.add(new Node());
        }
        
        for(int ij = 0; ij < numLinks; ij++)
        {
            Node i = null;
        
            do
            {
                i = nodes.get((int)Math.floor((nodes.size()-1) * Math.random()));
                //System.out.println("i "+i.getId()+" "+i.getOutgoing().size());
            }
            while(i.getOutgoing().size() >= nodes.size() - i.getId());
            
            Node j = null;
            int j_id = 0;
            
            do
            {
                j_id = (int)Math.floor((i.getId()) + (nodes.size() - (i.getId()))* Math.random());
                j = nodes.get(j_id);
                
                //System.out.println("ij "+i.getId()+" "+j.getId()+" "+i.getOutgoing().size());
            }
            while(i.getId() >= j.getId() || i.hasLink(j));
            
            links.add(new Link(this, i, j, 3));
        }
    }
    
    public String getFilename()
    {
        return "rand "+nodes.size()+"-"+links.size()+"-"+num_scenarios+" with "+countLinks()+" links.txt";
    }
}
