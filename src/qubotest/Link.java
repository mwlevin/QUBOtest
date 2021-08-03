/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qubotest;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;

/**
 *
 * @author micha
 */
public class Link implements Comparable<Link>
{
    public int cost;
    public int arr_time = 0;
    
    private Node source, dest;
    
    private int[][] costs;
    
    private IloIntVar[][] x;
    
    public Link(Network network, Node source, Node dest)
    {
        this.source = source;
        this.dest = dest;
        
        source.addOutgoing(this);
        dest.addIncoming(this);
        
        costs = new int[network.num_scenarios][network.T];
        x = new IloIntVar[network.num_scenarios][network.T];
    }
    
    public Link(Network network, Node start, Node end, int b)
    {
        this.source = start;
        this.dest = end;
        
        start.addOutgoing(this);
        end.addIncoming(this);
        
        costs = new int[network.num_scenarios][network.T];
        x = new IloIntVar[network.num_scenarios][network.T];
        
        // randomly generate tts
        for(int k = 0; k < costs.length; k++)
        {
            for(int t = 0; t < costs[k].length; t++)
            {
                int min = 1;
                int max = b;
                
                if(t > 0)
                {
                    //min = tts[k][t-1]-1;
                }
                
                
                
                int w = min + (int)Math.round(Math.random() * (max-min));
                //int w = 1;
                
                if(w + t < network.T)
                {
                    costs[k][t] = w;
                }
            }
        }
    }
    
    public int getTT(int k, int t)
    {
        return costs[k][t];
    }
    
    public int compareTo(Link rhs)
    {
        if(source != rhs.source)
        {
            return source.getId() - rhs.source.getId();
        }
        else if(dest != rhs.dest)
        {
            return dest.getId() - rhs.dest.getId();
        }
        return 0;
    }
    
    public IloIntVar getX(Scenario s, int t)
    {
        return x[s.getIdx()][t];
    }
    
    public void init(IloCplex cplex) throws IloException
    {
        for(int k = 0; k < x.length; k++)
        {
            for(int t = 0; t < x[k].length; t++)
            {
                x[k][t] = cplex.intVar(0, 1);
            }
        }
    }
    
    public void setW(Scenario s, int t, int w)
    {
        costs[s.getIdx()][t] = w;
    }
    
    public Node getSource()
    {
        return source;
    }
    public Node getDest()
    {
        return dest;
    }
    
    public int getW(Scenario s, int t)
    {
        return costs[s.getIdx()][t];
    }
    
    public int hashCode()
    {
        return source.getId()*1000 + dest.getId();
    }
    
    public boolean equals(Object o)
    {
        Link rhs = (Link)o;
        
        return rhs.source.equals(source) && rhs.dest.equals(dest);
    }
    
    public String toString()
    {
        return "["+source.getId()+","+dest.getId()+"]";
    }
}
