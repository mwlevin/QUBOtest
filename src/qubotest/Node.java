/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qubotest;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author micha
 */
public class Node 
{
    public int cost;
    public int arr_time = 0;
    
    private int id;
    public static int next_id = 1;
    
    private Set<Link> incoming, outgoing;
    
    public Node()
    {
        this(next_id++);
    }
    
    public Node(int id)
    {
        this.id = id;
        
        incoming = new HashSet<>();
        outgoing = new HashSet<>();
    }
    
    public boolean hasLink(Node j)
    {
        for(Link ij : outgoing)
        {
            if(ij.getDest()== j)
            {
                return true;
            }
        }
        
        return false;
    }
    public Set<Link> getIncoming()
    {
        return incoming;
    }
    
    public Set<Link> getOutgoing()
    {
        return outgoing;
    }
    
    public void addIncoming(Link l)
    {
        incoming.add(l);
    }
    
    public void addOutgoing(Link l)
    {
        outgoing.add(l);
    }
    
    public int getId()
    {
        return id;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public String toString()
    {
        return ""+id;
    }
    public boolean equals(Object o)
    {
        Node rhs = (Node)o;
        return rhs.id == id;
    }
}
