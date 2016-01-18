import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import java.io.*;
import java.awt.*;
import java.util.StringTokenizer;

public class Techind extends JDialog{
  public Techind(final CPL cpl, String indvalue){
    
    boolean stop = false;
    int counter = 0;
    float test = 0;
    StringTokenizer st = new StringTokenizer(indvalue,"#");
    while (st.hasMoreTokens()){
      StringTokenizer x = new StringTokenizer(st.nextToken(),"@\"");
      // x has two tokens: company and a set of pattern instances
      String companyName = x.nextToken();
      //String formula = x.nextToken();
      int SizeOfArr = Integer.parseInt(x.nextToken());
      
      StringTokenizer y = new StringTokenizer(x.nextToken(),",");      
      float ti[] = new float[SizeOfArr];
      
      for(int i = 0; i<SizeOfArr; i++){
	ti[i] = Float.parseFloat(y.nextToken());
      }
      
      cpl.graph.loadChart("data/ascii/"+companyName);
      cpl.graph.addALineWithLocAndVal(ti, SizeOfArr);
    }
  }
}
