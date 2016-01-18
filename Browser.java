import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import java.io.*;
import java.awt.*;
import java.util.StringTokenizer;

public class Browser extends JDialog{
    public Browser(final CPL cpl, String patterns){
	super(cpl,false);

	DefaultMutableTreeNode top =
	    new DefaultMutableTreeNode("Pattern Instances:");
	//createNodes(top);
	final JTree tree = new JTree(top);
	JScrollPane treeView = new JScrollPane(tree);
	treeView.setPreferredSize(new Dimension(200,200));
	getContentPane().add(treeView, BorderLayout.CENTER);

	tree.getSelectionModel().setSelectionMode
	    (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new TreeSelectionListener(){
		public void valueChanged(TreeSelectionEvent e){
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
			tree.getLastSelectedPathComponent();
		    if (node == null) return;
		    Object nodeInfo = node.getUserObject();
		    if (node.isLeaf()){
			PatternInstance p = (PatternInstance) nodeInfo;
			cpl.graph.loadChart("data/ascii/"+p.companyName);
			for(int i=0; i < (p.numberOfLMS-1);i++){
			    cpl.graph.addALine(p.landmarks[i],p.landmarks[i+1]);
			}
			String s = ((PatternInstance)nodeInfo).toString();
			//System.out.println(s);
		    }
		}
	    });

        createTree(top,patterns);	
	pack();
	setVisible(true);
	
    }

    public void createTree(DefaultMutableTreeNode top, String s){
	DefaultMutableTreeNode company = null;
	DefaultMutableTreeNode pattern = null;
	
	//System.out.println(s);
		
	StringTokenizer st = new StringTokenizer(s,"#");
	while (st.hasMoreTokens()){
	    StringTokenizer x = new StringTokenizer(st.nextToken(),"@");
	    // x has two tokens: company and a set of pattern instances
	    String companyName = x.nextToken();
	    //System.out.println(companyName);
            company = new DefaultMutableTreeNode(companyName);
	    //String t = x.nextToken();
	    //System.out.println(t);
	    StringTokenizer y = new StringTokenizer(x.nextToken(),"%");

	    while(y.hasMoreTokens()){
		pattern = new DefaultMutableTreeNode(
			       new PatternInstance(y.nextToken(),companyName));
		company.add(pattern);
	    }
	    top.add(company);
	}
    }

    private class PatternInstance{
	int[] landmarks = new int[20];
	int numberOfLMS=0;
	String companyName;

	PatternInstance(String s, String company){
	    companyName = new String(company);
	    int i=0;
	    StringTokenizer z = new StringTokenizer(s,",");
	    while (z.hasMoreTokens()){
		landmarks[i++] =  Integer.parseInt(z.nextToken());
	    }
            numberOfLMS = i;
	    //System.out.print(i);
	}

	public String toString(){
	    String temp = new String("");
	    //System.out.println(numberOfLMS);
	    for(int i=0;i<numberOfLMS;i++){
		temp = temp + String.valueOf(landmarks[i]) + " - ";
	    }
	    //System.out.println("temp "+ temp);
	    return(temp.substring(0,temp.length()-3));
	}

	
    }

}