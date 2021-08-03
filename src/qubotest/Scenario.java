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
public class Scenario 
{
    private int id;
    
    private int idx;
    public static int next_idx = 0;
    
    public Scenario(int id)
    {
        this.id = id;
        idx = next_idx++;
    }
    
    public int getIdx()
    {
        return idx;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public boolean equals(Object o)
    { 
        Scenario rhs = (Scenario)o;
        
        return rhs.id == id;
    }
    
    public int getId()
    {
        return id;
    }
    
    public String toString()
    {
        return ""+id;
    }
}
