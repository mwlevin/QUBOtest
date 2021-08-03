/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qubotest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *
 * @author micha
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("grid/solutions.txt")), true);
        
        fileout.println("size\tK\tT\tinstance\tObj value\tCPLEX time (s)\tOptimal path");
        
        int repeat = 10;
        boolean acyclic = true;
        
        for(int n = 3; n <= 7; n++)
        {
            for(int K = 2; K <= 6; K+=2)
            {
                for(int T = 30; T <= 90; T+= 30)
                {
                    for(int x = 0; x < repeat; x++)
                    {
                        GridNetwork test;

                        //System.out.print("Looping for connectivity...");
                        
                        do
                        {
                            test = new GridNetwork(true, n, n, T, K);
                        }
                        while(!test.isConnected());
                        
                        //System.out.println("done.");

                        //System.out.println(test.isConnected());


                        test.printNetwork(new File(
                        "grid/"+(acyclic? "A":"N")+n+"x"+n+"_T="+T+"_K="+K+"-"+x+".txt"
                        ));
                        
                        test.solve();
                        
                        fileout.print(n+"\t"+K+"\t"+T+"\t"+x+"\t"+test.getObj()+"\t"+test.getCplexTime()+"\t");
                        
                        for(Node optn : test.getPath())
                        {
                            fileout.print(optn+" ");
                        }
                        fileout.println();
                        
                    }

                }
            }

        }
        
        fileout.close();
        
       //Network network = new Network("SmallPrev");
       
       //network.solveLagrangian(1, 4);
       //network.solve(1, 4);
       
       /*
        RandomNetwork test = null;
        
        do
        {
            test = new RandomNetwork(12, 20, 20, 3);

            System.out.println(test.isConnected());
        }
        while(!test.isConnected());
        
        test.printNetwork();
        */
        
        /*
        GridNetwork test = new GridNetwork(true, 10, 10, T, 30);
        
        System.out.println(test.isConnected());

        if(test.isConnected())
        {
            test.printNetwork();
        }
        */
    }
    
}
