/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qubotest;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloQuadIntExpr;
import ilog.cplex.IloCplex;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author micha
 */
public class Network 
{
    public int num_scenarios;
    public int T;
    
    private String name;
    
    protected List<Node> nodes;
    protected List<Link> links;
    protected List<Scenario> scenarios;
    
    public Network(int T, int num_scenarios)
    {
        this.T = T;
        this.num_scenarios = num_scenarios;
        
        nodes = new ArrayList<>();
        links = new ArrayList<>();
        scenarios = new ArrayList<>();
        
        for(int k = 0; k < num_scenarios; k++)
        {
            scenarios.add(new Scenario(k+1));
        }
        
        Node.next_id = 1;
    }
    
    public Network(String name) throws IOException
    {
        this.name = name;
        
        nodes = new ArrayList<>();
        links = new ArrayList<>();
        scenarios = new ArrayList<>();
        
        
        
        Scanner filein = new Scanner(new File(name+".txt"));
        
        filein.nextLine();
        
        while(filein.hasNext())
        {
            int k = filein.nextInt();
            int i = filein.nextInt();
            int j = filein.nextInt();
            int t = filein.nextInt();
            int w = filein.nextInt();
            
            Link link = findLink(i, j);
            Scenario scenario = findScenario(k);
            
            link.setW(scenario, t, w);
            
            T = (int)Math.max(t, T);
        }

        filein.close();
        
        num_scenarios = scenarios.size();
        
    }
    
    public String getFilename()
    {
        return name+".txt";
    }
    
    public int countLinks()
    {
        int count = 0;
        for(int k = 0; k < num_scenarios; k++)
        {
            for(int t = 0; t < T; t++)
            {
                for(Link ij : links)
                {

                    int tt = ij.getTT(k, t);
                    
                    if(tt > 0)
                    {
                        count++;

                    }
                }
            }
        }
        
        return count;
    }
    
    public void printNetwork() throws IOException
    {
        printNetwork(new File(getFilename()));
    }
    public void printNetwork(File file) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        
        Collections.sort(links);
        
        int count = 0;
        
        double pr = 1.0/num_scenarios;
        
        fileout.println("k, i, j, t, w, pr");
        
        for(int k = 0; k < num_scenarios; k++)
        {
            for(int t = 0; t < T; t++)
            {
                for(Link ij : links)
                {

                    int tt = ij.getTT(k, t);
                    
                    if(tt > 0)
                    {
                        count++;
                        fileout.println((k+1)+", "+ij.getSource().getId()+", "+ij.getDest().getId()+", "+
                                t+", "+tt+", "+String.format("%.2f",pr));
                    }
                }
            }
        }
        
        fileout.close();
        
        System.out.println(count);
    }
    
    public void dijkstras(int k, Node source)
    {
        for(Node n : nodes)
        {
            n.cost = 1000000;
        }
        
        source.cost = 0;
        source.arr_time = 0;
        
        Set<Node> Q = new HashSet<>();
        
        Q.add(source);
        
        while(!Q.isEmpty())
        {
            Node u = null;
            int best = 1000000;
            
            for(Node n : Q)
            {
                if(n.cost < best)
                {
                    best = n.cost;
                    u = n;
                }
            }
            
            Q.remove(u);

            
            for(Link uv : u.getOutgoing())
            {
                Node v = uv.getDest();
                
                if(u.arr_time >= T)
                {
                    continue;
                }
                
                int tt = uv.getTT(k, u.arr_time);
                
                
                
               
                
                if(tt > 0 && u.cost + tt < v.cost)
                {
                    v.cost = u.cost + tt;
                    v.arr_time = u.arr_time + tt;
                    Q.add(v);
                }
            }
        }
        
    }
    
    public boolean isConnected()
    {
        boolean output = true;
        
        for(int k = 0; k < num_scenarios; k++)
        {
            dijkstras(k, nodes.get(0));
            
            output = output & nodes.get(nodes.size()-1).cost < 100000;
        }
        
        return output;
    }
    
    public void solve(int origin, int dest) throws IloException
    {
        IloCplex cplex = new IloCplex();
        
        for(Link l : links)
        {
            l.init(cplex);
        }
        
        IloLinearNumExpr obj = cplex.linearNumExpr();
        
        for(Scenario s : scenarios)
        {
            for(int t = 0; t < T; t++)
            {
                for(Link l : links)
                {
                    obj.addTerm(l.getX(s, t), l.getW(s, t));
                }
            }
        }
        
        cplex.addMinimize(obj);
        
        
        Node on = findNode(origin);
        
        for(Scenario s : scenarios)
        {
            IloLinearNumExpr lhs = cplex.linearNumExpr();
            
            for(Link l : on.getOutgoing())
            {
                lhs.addTerm(l.getX(s, 0), 1);
            }
            
            cplex.addEq(lhs, 1);
        }
        
        Node dn = findNode(dest);
        
        for(Scenario s : scenarios)
        {
            IloLinearNumExpr lhs = cplex.linearNumExpr();
            
            for(int t = 0; t < T; t++)
            {
                for(Link l : dn.getIncoming())
                {
                    lhs.addTerm(l.getX(s, t), 1);
                }
            }
            
            cplex.addEq(lhs, 1);
        }
        
        for(Scenario k : scenarios)
        {
            for(Scenario l : scenarios)
            {
                if(k == l)
                {
                    continue;
                }
                
                for(Link ij : links)
                {
                    IloLinearNumExpr lhs = cplex.linearNumExpr();
                    IloLinearNumExpr rhs = cplex.linearNumExpr();
                    
                    for(int t = 0; t < T; t++)
                    {
                        lhs.addTerm(ij.getX(k, t), 1);
                        rhs.addTerm(ij.getX(l, t), 1);
                    }
                    
                    cplex.addEq(lhs, rhs);
                }
            }
        }
        
        for(Scenario s : scenarios)
        {
            for(Link ij : links)
            {
                if(ij.getDest() == dn)
                {
                    continue;
                }
                
                for(int t = 0; t < T; t++)
                {
                    IloLinearNumExpr rhs = cplex.linearNumExpr();
                    
                    for(Link jk : ij.getDest().getOutgoing())
                    {
                        rhs.addTerm(jk.getX(s, t + ij.getW(s, t)), 1);
                    }
                    
                    cplex.addEq(ij.getX(s, t), rhs);
                }
            }
        }
        
        
        
        cplex.solve();
        
        printSolution(cplex);
    }
    
    public void printSolution(IloCplex cplex) throws IloException
    {
        for(Scenario s : scenarios)
        {
            System.out.println("Scenario "+s);
            
            for(Link ij : links)
            {
                System.out.println("\tLink "+ij);
                
                for(int t = 0; t < T; t++)
                {
                    System.out.println("\t\t"+t+": "+(cplex.getValue(ij.getX(s, t)) == 1? "1\t"+ij.getW(s, t):""));
                }
            }
        }
    }
    
    public void solveLagrangian(int origin, int dest) throws IloException, IOException
    {
        
        int lagrangeS = 1000;
        int lagrangeD = 1000;
        int lagrangeL = 1000;
        int lagrangeQ = 1000;
        
        
        IloCplex cplex = new IloCplex();
        
        for(Link l : links)
        {
            l.init(cplex);
        }
        
        obj = cplex.linearNumExpr();
        
        for(Scenario s : scenarios)
        {
            for(int t = 0; t < T; t++)
            {
                for(Link l : links)
                {
                    addTerm(l.getW(s, t), new X(l, s, t));
                    //obj.addTerm(l.getW(s, t), l.getX(s, t));
                }
            }
        }


        objL = cplex.quadIntExpr();
        
        Node on = findNode(origin);
        Node dn = findNode(dest);
        
        for(Scenario s : scenarios)
        {
            for(Link si : on.getOutgoing())
            {
                
                
                for(Link sip : on.getOutgoing())
                {
                    if(si == sip)
                    {
                        continue;
                    }
                    
                    addTerm(lagrangeS, new X(si, s, 0), new X(sip, s, 0));
                    //objL.addTerm(lagrangeS, si.getX(s, 0), sip.getX(s, 0));
                    
                    //System.out.println(si+"*"+sip);
                }
                
                addTerm(-lagrangeS, new X(si, s, 0));
                
            }
        }
        
        
        for(Scenario s : scenarios)
        {
            for(Link id : dn.getIncoming())
            {
                for(int t = 0; t < T; t++)
                {
                    addTerm(-lagrangeD, new X(id, s, t));
                    //obj.addTerm(-lagrangeD, id.getX(s, t));
                    
                    for(Link idp : dn.getIncoming())
                    {
                        for(int tp = 0; tp < T; tp++)
                        {
                            if(id == idp && t == tp)
                            {
                                continue;
                            }
                            
                            addTerm(lagrangeD, new X(id, s, t), new X(idp, s, tp));
                            //objL.addTerm(lagrangeD, id.getX(s, t), idp.getX(s, tp));
                        }
                    }
                }
                
                
            }
        }
        
        
        for(Scenario l : scenarios)
        {
            for(Scenario m : scenarios)
            {
                if(l.getIdx() >= m.getIdx())
                {
                    continue;
                }
                for(Link ij : links)
                {
                    for(int t = 0; t < T; t++)
                    {
                        addTerm(lagrangeL, new X(ij, l, t));
                        //obj.addTerm(lagrangeL, ij.getX(l, t));
                        addTerm(lagrangeL, new X(ij, m, t));
                        //obj.addTerm(lagrangeL, ij.getX(m, t));
                        
                        for(int tp = t+1; tp < T; tp++)
                        {
                            addTerm(2*lagrangeL, new X(ij, l, t), new X(ij, l, tp));
                            //objL.addTerm(2*lagrangeL, ij.getX(l, t), ij.getX(l, tp));
                            addTerm(2*lagrangeL, new X(ij, m, t), new X(ij, m, tp));
                            //objL.addTerm(2*lagrangeL, ij.getX(m, t), ij.getX(m, tp));
                        }
                        
                        for(int tp = 0; tp < T; tp++)
                        {
                            addTerm(-2*lagrangeL, new X(ij, l, t), new X(ij, m, tp));
                            //objL.addTerm(-2*lagrangeL, ij.getX(l, t), ij.getX(m, tp));
                        }
                        
                    }
                }
            }
        }
        
        for(Scenario s : scenarios)
        {
            for(int t = 0; t < T; t++)
            {
                for(Link ij : links)
                {
                    if(ij.getDest().equals(dn))
                    {
                        continue;
                    }
                    
                    addTerm(lagrangeQ, new X(ij, s, t));
                    //obj.addTerm(lagrangeQ, ij.getX(s, t));
                    
                    int dtime = t+ij.getW(s, t);
                    
                    for(Link jl : ij.getDest().getOutgoing())
                    {
                        addTerm(lagrangeQ, new X(jl, s, dtime));
                        //obj.addTerm(lagrangeQ, jl.getX(s, dtime));
                        
                        addTerm(-2*lagrangeQ, new X(ij, s, t), new X(jl, s, dtime));
                        //objL.addTerm(-2*lagrangeQ, ij.getX(s, t), jl.getX(s, dtime));
                        
                        
                        for(Link jlp : ij.getDest().getOutgoing())
                        {
                            if(jlp == jl)
                            {
                                continue;
                            }
                            
                            addTerm(lagrangeQ, new X(jl, s, dtime), new X(jlp, s, dtime));
                            //objL.addTerm(lagrangeQ, jl.getX(s, dtime), jlp.getX(s, dtime));
                        }
                    }
                    
                    
                }
            }
        }
        
        
        
        printCoefficients();
        
        
        cplex.addMinimize(cplex.sum(obj, objL));
        
        cplex.setParam(IloCplex.Param.TimeLimit, 60*5);
        cplex.solve();
        
        printSolution(cplex);
    }
    
    private IloLinearNumExpr obj;
    private IloQuadIntExpr objL;
    private HashMap<X, HashMap<X, Integer>> coeff = new HashMap<>();
    
    public void printCoefficients() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("coeff_"+name+".txt")), true);
        
        fileout.println("i\tj\tk\tt\ti2\tj2\tk2\tt2\tcoeff");
        
        for(X x1 : coeff.keySet())
        {
            for(X x2 : coeff.get(x1).keySet())
            {
                fileout.println(x1.ij.getSource()+"\t"+x1.ij.getDest()+"\t"+x1.k+"\t"+x1.t+"\t"+
                    x2.ij.getSource()+"\t"+x2.ij.getDest()+"\t"+x2.k+"\t"+x2.t+"\t"+
                    coeff.get(x1).get(x2));
            }
        }
    }
    
    
    public void addTerm(int c, X x1) throws IloException
    {
        addTerm(c, x1, x1);
    }
    public void addTerm(int c, X x1, X x2) throws IloException
    {
        if(x1.equals(x2))
        {
            obj.addTerm(c, x1.getX());
        }
        else
        {
            objL.addTerm(c, x1.getX(), x2.getX());
        }
        
        if(coeff.containsKey(x1))
        {
            HashMap<X, Integer> temp = coeff.get(x1);
            
            if(temp.containsKey(x2))
            {
                temp.put(x2, temp.get(x2)+c);
            }
            else
            {
                temp.put(x2, c);
            }
        }
        else if(coeff.containsKey(x2))
        {
            HashMap<X, Integer> temp = coeff.get(x2);
            
            if(temp.containsKey(x1))
            {
                temp.put(x1, temp.get(x1)+c);
            }
            else
            {
                temp.put(x1, c);
            }
        }
        else
        {
            HashMap<X, Integer> temp = new HashMap<>();
            
            temp.put(x2, c);
            coeff.put(x1, temp);
        }
    }
    
    
    public Scenario findScenario(int k)
    {
        for(Scenario s : scenarios)
        {
            if(s.getId() == k)
            {
                return s;
            }
        }
        
        Scenario output = new Scenario(k);
        scenarios.add(output);
        return output;
    }
    
    public Link findLink(int i, int j)
    {
        Node in = findNode(i);
        Node jn = findNode(j);
        
        for(Link l : in.getOutgoing())
        {
            if(l.getDest().equals(jn))
            {
                return l;
            }
        }
        
        Link output = new Link(this, in, jn);
        links.add(output);
        return output;
    }
    
    public Node findNode(int i)
    {
        for(Node n : nodes)
        {
            if(n.getId() == i)
            {
                return n;
            }
        }
        
        Node output = new Node(i);
        nodes.add(output);
        return output;
    }
    
}
