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
public class GridNetwork extends Network
{
    private int rows, cols;
    
    
    
    public GridNetwork(boolean acyclic, int rows, int cols, int T, int num_scenarios)
    {
        super(T, num_scenarios);
        
        
        
        
        
        this.rows = rows;
        this.cols = cols;
        
        Node[][] mat = new Node[rows][cols];
        
        for(int r = 0; r < rows; r++)
        {
            for(int c = 0; c < cols; c++)
            {
                Node temp = new Node();
                mat[r][c] = temp;
                nodes.add(temp);
            }
        }
        
        origin = 1;
        dest = Node.next_id-1;
        
        int n = (rows + cols)/2;
        int b = T/(n-1)-1;
        //b = 1;
        //System.out.println(n+" "+b);
        
        for(int r = 0; r < rows; r++)
        {
            for(int c = 0; c < cols; c++)
            {
                if(c < cols-1)
                {
                    
                    
                    links.add(new Link(this, mat[r][c], mat[r][c+1], b));
                    
                    if(!acyclic)
                    {
                        links.add(new Link(this, mat[r][c+1], mat[r][c], b));
                    }
                    
                }
                if(r < rows-1)
                {
                    links.add(new Link(this, mat[r][c], mat[r+1][c], b));
                    
                    if(!acyclic)
                    {
                        links.add(new Link(this, mat[r+1][c], mat[r][c], b));
                    }
                }
                
                
            }
        }
    }
    
    public String getFilename()
    {
        return "grid "+rows+"x"+cols+"-"+num_scenarios+" with "+countLinks()+" links.txt";
    }
    
    
}
