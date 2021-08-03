/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qubotest;

import ilog.concert.IloIntVar;

/**
 *
 * @author micha
 */
public class X 
{
    public Link ij;
    public Scenario k;
    public int t;
    
    public X(Link ij, Scenario k, int t)
    {
        this.ij = ij;
        this.k = k;
        this.t = t;
    }
    
    public boolean equals(Object o)
    {
        X rhs = (X)o;
        
        return ij == rhs.ij && k == rhs.k && t == rhs.t;
    }
    
    public int hashCode()
    {
        return ij.hashCode()*100 + k.getIdx()*10000 + t;
    }
    public IloIntVar getX()
    {
        return ij.getX(k, t);
    }
}
