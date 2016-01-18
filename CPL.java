import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.File;
import java.io.*;

public class CPL extends JFrame{
  final Graph graph = new Graph();
  final CommandLine cmdLine = new CommandLine(this);
  JFrame parent=this;
  String currentfile = "";
  String currentpath = "";
  String currentcmp = "";
  String cmd_ind = "";
  String[] cmds = new String[5];
  Process child=null;

  public CPL(){
    super("Charting Patterns on Price History");
    final Container pane = getContentPane();
    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {System.exit(0);}
      });
    graph.setPreferredSize(new Dimension(800,400));
    graph.setBorder(BorderFactory.createEtchedBorder());
    
    //final CommandLine cmdLine = new CommandLine(this);
    cmdLine.setPreferredSize(new Dimension(800,200));
    
    //create the menu bar
    JMenuBar menuBar = new JMenuBar();
    //setJMenuBar(menuBar);

    //ceate the first menu (file)
    JMenu mFile = new JMenu("File");
    mFile.setMnemonic(KeyEvent.VK_F);
    mFile.getAccessibleContext().setAccessibleDescription("File Related");
    menuBar.add(mFile);

    //a group of JMenuItems
    /*********************************
        Open & Load Original Data 
    **********************************/
    JMenuItem mif_open = new JMenuItem("Open", new ImageIcon("images/open.gif"));
    mif_open.setMnemonic(KeyEvent.VK_O);
    mFile.add(mif_open);
    
    mif_open.setToolTipText("Open a chart");
    mif_open.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  JFileChooser chooser = new JFileChooser(".//data//ascii");
	  chooser.addChoosableFileFilter(new DataFilter());
	  chooser.setAcceptAllFileFilterUsed(false);
	  
	  int returnVal = chooser.showOpenDialog(pane);
	  if(returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = chooser.getSelectedFile();
	    graph.loadChart(file.getAbsolutePath());
	    graph.IndicatorNo = 0;
	    graph.resetInit();
	    cmdLine.setActiveChart(file.getPath());
	    //System.out.println(file.getPath());
	    currentfile = file.getName();
	    currentpath = file.getPath();
	    StringTokenizer st = new StringTokenizer(currentfile, ".");
	    currentcmp = st.nextToken();		    
	  }
	}
      });
    
    /**************************************
                  Exit System
     **************************************/
    mFile.addSeparator();
    JMenuItem mif_exit = new JMenuItem("Exit");
    mif_exit.setMnemonic(KeyEvent.VK_E);
    mFile.add(mif_exit);

    mif_exit.setToolTipText("Exit CPL System");
    mif_exit.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  cmdLine.runHaskellCmd(":q\n");
	  System.exit(0);
	}
      });

    //ceate the second menu (View)
    JMenu mView = new JMenu("View");
    mView.setMnemonic(KeyEvent.VK_V);
    mView.getAccessibleContext().setAccessibleDescription("View Related");
    menuBar.add(mView);

    //a group of JMenuItems
    /*************************************
           Zoom In Button
    *************************************/
    JMenuItem miv_in = new JMenuItem("ZoomIn", new ImageIcon("images/zoomin.gif"));
    miv_in.setMnemonic(KeyEvent.VK_I);
    mView.add(miv_in);
    
    miv_in.setToolTipText("Expand Time Axis");
    miv_in.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if (graph.gap < 10){
	    graph.gap = graph.gap+1;
	    graph.repaint();
	  }
	}
      });
    
    /**********************************
         Zoom Out Button
     **********************************/
    JMenuItem miv_out = new JMenuItem("ZoomOut", new ImageIcon("images/zoomout.gif"));
    miv_out.setMnemonic(KeyEvent.VK_O);
    mView.add(miv_out);

    miv_out.setToolTipText("Compress Time Axis");
    miv_out.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if (graph.gap > 1){
	    graph.gap = graph.gap-1;
	    graph.repaint();
	  }
	}
      });
    
    //ceate the third menu (Indicator)
    JMenu mIndicator = new JMenu("Indicator");
    mIndicator.setMnemonic(KeyEvent.VK_I);
    mIndicator.getAccessibleContext().setAccessibleDescription("Indicator Related");
    menuBar.add(mIndicator);

    //a group of JMenuItems
    /**************************************
        Create New Indicator Definition
     **************************************/
    JMenuItem mii_new = new JMenuItem("New");
    mii_new.setMnemonic(KeyEvent.VK_N);
    mIndicator.add(mii_new);

    mii_new.setToolTipText("Create New Indicator");
    mii_new.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  MyEditor dlg = new MyEditor("Indicator");
	  dlg.setSize(600, 600);
	  dlg.setTitle("New");
	  dlg.show();
	  String cmd = dlg.dlg.getFileName();
	  if(!cmd.equals("")){
	    cmdLine.runHaskellCmd(":r\n");    
	  }  
	}
      });
    
    /**************************************
      Delete User Created Indicator Button
     **************************************/
    JMenuItem mii_delete = new JMenuItem("Delete");
    mii_delete.setMnemonic(KeyEvent.VK_D);
    mIndicator.add(mii_delete);
        
    mii_delete.setToolTipText("Delete Indicator");
    mii_delete.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  MySelect dlg = new MySelect("Delete", "Indicator");
	  dlg.setTitle("Select Indicator");
	  dlg.setSize(new Dimension(300, 200));
	  dlg.show();
	  String cmd = dlg.dlg.getFileName();
	  if(!cmd.equals("")){
	    cmdLine.runHaskellCmd(":r\n");    
	  } 
	}
      });

    /**************************************
      Modify User Created Indicator Button
     **************************************/
    JMenuItem mii_modify = new JMenuItem("Modify");
    mii_modify.setMnemonic(KeyEvent.VK_M);
    mIndicator.add(mii_modify);

    mii_modify.setToolTipText("Modify Indicator");
    mii_modify.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  JFileChooser chooser = new JFileChooser(".//Indicators");
	  chooser.addChoosableFileFilter(new IndicatorFilter());
	  chooser.setAcceptAllFileFilterUsed(false);
	  
	  int returnVal = chooser.showOpenDialog(pane);
	  if(returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = chooser.getSelectedFile();
	    currentpath = file.getPath();
	    try{
                child = Runtime.getRuntime().exec("D:\\emacs-21.3\\bin\\runemacs.exe " + currentpath);

	    }catch(IOException runerror){
	      System.err.println("Test!");
	    }
	  }
	  /*
	  MySelect dlg = new MySelect("Modify", "Indicator");
	  dlg.setTitle("Select Indicator");
	  dlg.setSize(new Dimension(300, 200));
	  dlg.show();
	  String cmd = dlg.dlg.getFileName();
	  if(!cmd.equals("")){
	    cmdLine.runHaskellCmd(":r\n");    
	  }
	  */
	}
      });
    /**************************************
      View User Created Indicators
     **************************************/
    JMenuItem mii_view = new JMenuItem("View");
    mii_view.setMnemonic(KeyEvent.VK_V);
    mIndicator.add(mii_view);

    mii_view.setToolTipText("View Indicator");
    mii_view.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  MySelect dlg = new MySelect("View", "Indicator");
	  dlg.setTitle("Select Indicator");
	  dlg.setSize(new Dimension(300, 200));
	  dlg.show();
	}
      });
    
    /**************************************
              View Indicator Graph
     **************************************/
    JMenuItem mii_viewg = new JMenuItem("View Graph ...");
    mii_viewg.setMnemonic(KeyEvent.VK_C);
    mIndicator.add(mii_viewg);

    mii_viewg.setToolTipText("Combine View Indicator");
    mii_viewg.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if(graph.hasInd()==false){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 0);
	  }
	  else{
	    int IndicatorNo = graph.getIndicatorNo();
	    MyIndSaver dlg = new MyIndSaver(currentpath, IndicatorNo, graph.getIndNames(), "view");		    
	    dlg.setSize(400, 300);
	    dlg.setTitle("View Indicator Graph");
	    dlg.show();
	    if(dlg.actived){
	      int[] choice = dlg.getChoice();
	      String[] ti_names = dlg.getIndNames();
	      MyDate[] ti_date = graph.getDate();
	      float[][] ti_datas = graph.getTI();
	      int noBars = graph.noOfPoints;	      
	      combineIndView(ti_datas, ti_date, ti_names, noBars, choice, IndicatorNo);
	    }
	  }
	}
      });
    
    /**************************************
         Eval Sub Menu
     **************************************/
    JMenu mind_sub_eval = new JMenu("Eval");
    mind_sub_eval.setMnemonic(KeyEvent.VK_E);

    //create the submenu of the menu Indicator (System)
    //mIndicator.addSeparator();
    JMenu mind_sub_system = new JMenu("System Indicators");
    mind_sub_system.setMnemonic(KeyEvent.VK_S);
    
    /*****************************************
                 Moving Average
    *****************************************/
    JMenuItem mii_movingavg = new JMenuItem("Moving Average");
    mii_movingavg.setMnemonic(KeyEvent.VK_M);
    mind_sub_system.add(mii_movingavg);
    
    mii_movingavg.setToolTipText("Calculate Indicator");
    mii_movingavg.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if(graph.IndicatorNo>=5){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 2);
	    //dlg.show();
	  }
	  else if(currentfile.equals("")){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 1);
	    //dlg.show();
	  }   
	  else{	
	    MyMovingAvg dlg = new MyMovingAvg(parent);
	    dlg.setSize(600, 200);
	    dlg.setTitle("Moving Average Parameters");
	    dlg.show();
	    
	    if(dlg.dlg.thereIsInd){
	      String Ind_fun = "movingAvg";
	      String base_day = dlg.dlg.basedays_s;
	      String base_value = dlg.dlg.basevalue_s;
	      String cmd = Ind_fun + " " + base_day + " " + base_value;
	      graph.setIndFunc(cmd);
	      cmd_ind = "evalDisplay \""+ currentcmp+"/"+currentfile + "\" (" + cmd + ")\n";  
	      cmdLine.runHaskellCmd(cmd_ind);
	    }
	  }
	}
      });
    
    /*
    JMenuItem mii_tprice = new JMenuItem("Tprice");
    mii_tprice.setMnemonic(KeyEvent.VK_T);
    mind_sub_system.add(mii_tprice);
    
    mii_tprice.setToolTipText("Calculate Indicator");
    mii_tprice.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if(graph.IndicatorNo>=5){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 2);
	    //dlg.show();
	  }
	  else if(currentfile.equals("")){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 1);
	    //dlg.show();
	  }   
	  else{
	    graph.setIndFunc("tprice");
	    cmd_ind = "evalDisplay \""+ currentcmp+"/"+currentfile + "\" tprice\n";	    		     
	    cmdLine.runHaskellCmd(cmd_ind);
	  }
	}
      });
    */
    mind_sub_eval.add(mind_sub_system);

    //create the menu item of the menu Indicator (User)
    JMenuItem mii_user = new JMenuItem("User Defined Indicators");
    mii_user.setMnemonic(KeyEvent.VK_U);
    mind_sub_eval.add(mii_user);
    
    mii_user.setToolTipText("User Defined Indicator");
    mii_user.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if(graph.IndicatorNo>=5){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 2);
	    //dlg.show();
	  }
	  else if(currentfile.equals("")){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 1);
	    //dlg.show();
	  }   
	  else{	
	    MySelectAction dlg = new MySelectAction(0);
	    dlg.setTitle("Select Indicator");
	    dlg.setSize(new Dimension(500, 300));
	    dlg.show();
	    if(dlg.dlg.getFunc()!= ""){
	      graph.setIndFunc(dlg.dlg.getFunc());
	      cmd_ind = "evalDisplay \""+ currentcmp+"/"+currentfile + "\" " + dlg.dlg.getFunc() + "\n";  
	      cmdLine.runHaskellCmd(cmd_ind);
	    }
	  }
	}
      });
   
    mIndicator.add(mind_sub_eval);
 
    //ceate the fourth menu (Pattern)
    JMenu mPattern = new JMenu("Pattern");
    mPattern.setMnemonic(KeyEvent.VK_P);
    mPattern.getAccessibleContext().setAccessibleDescription("Pattern Related");
    menuBar.add(mPattern);

    //a group of JMenuItems
    /**************************************
        Create New Pattern Definition
     **************************************/
    JMenuItem mip_new = new JMenuItem("New");
    mip_new.setMnemonic(KeyEvent.VK_N);
    mPattern.add(mip_new);

    mip_new.setToolTipText("Create New Pattern");
    mip_new.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  MyEditor dlg = new MyEditor("Pattern");
	  dlg.setSize(600, 600);
	  dlg.setTitle("New");
	  dlg.show();
	  String cmd = dlg.dlg.getFileName();
	  if(!cmd.equals("")){
	    cmdLine.runHaskellCmd(":r\n");    
	  }  
	}
      });
    
    /**************************************
      Delete User Created Pattern
     **************************************/
    JMenuItem mip_delete = new JMenuItem("Delete");
    mip_delete.setMnemonic(KeyEvent.VK_D);
    mPattern.add(mip_delete);

    mip_delete.setToolTipText("Delete Pattern");
    mip_delete.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  MySelect dlg = new MySelect("Delete", "Pattern");
	  dlg.setTitle("Select Pattern");
	  dlg.setSize(new Dimension(300, 200));
	  dlg.show();
	  String cmd = dlg.dlg.getFileName();
	  if(!cmd.equals("")){
	    cmdLine.runHaskellCmd(":r\n");    
	  } 
	}
      });
    
    /**************************************
      Modify User Created Pattern
     **************************************/
    JMenuItem mip_modify = new JMenuItem("Modify");
    mip_modify.setMnemonic(KeyEvent.VK_M);
    mPattern.add(mip_modify);
    
    mip_modify.setToolTipText("Modify Pattern");
    mip_modify.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  JFileChooser chooser = new JFileChooser(".//Patterns");
	  chooser.addChoosableFileFilter(new IndicatorFilter());
	  chooser.setAcceptAllFileFilterUsed(false);
	  
	  int returnVal = chooser.showOpenDialog(pane);
	  if(returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = chooser.getSelectedFile();
	    currentpath = file.getPath();
	    try{
	      child = Runtime.getRuntime().exec("D:\\emacs-21.3\\bin\\runemacs.exe " + currentpath);
	    }catch(IOException runerror){
	      System.err.println("Test!");
	    }
	  }
	  /*
	  MySelect dlg = new MySelect("Modify", "Pattern");
	  dlg.setTitle("Select Pattern");
	  dlg.setSize(new Dimension(300, 200));
	  dlg.show();
	  String cmd = dlg.dlg.getFileName();
	  if(!cmd.equals("")){
	    cmdLine.runHaskellCmd(":r\n");    
	  }
	  */
	}
      });
    
    /**************************************
      View User Created Pattern
     **************************************/
    JMenuItem mip_view = new JMenuItem("View");
    mip_view.setMnemonic(KeyEvent.VK_V);
    mPattern.add(mip_view);

    mip_view.setToolTipText("View Pattern");
    mip_view.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  MySelect dlg = new MySelect("View", "Pattern");
	  dlg.setSize(new Dimension(300, 200));
	  dlg.setTitle("Select Pattern");
	  dlg.show();
	}
      });
    
    /**************************************
       Browse Sub Menu
     **************************************/    
    JMenu mpat_sub_browse = new JMenu("Browse");
    mpat_sub_browse.setMnemonic(KeyEvent.VK_B);
    
    //create the submenu of the menu Indicator (System)
    //mPattern.addSeparator();
    JMenu mpat_sub_system = new JMenu("System Patterns");
    mpat_sub_system.setMnemonic(KeyEvent.VK_S);
    
    /***********************************
                    hdR
     ***********************************/
    JMenuItem mip_hdR = new JMenuItem("header & shoulder");
    mip_hdR.setMnemonic(KeyEvent.VK_R);
    mpat_sub_system.add(mip_hdR);

    mip_hdR.setToolTipText("Find hdR");
    mip_hdR.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if(currentfile.equals("")){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 1);
	    //dlg.show();
	  }
	  else{
	    String cmd = "browse hdR \""+ currentcmp+"/"+currentfile + "\"\n";
	    cmdLine.runHaskellCmd(cmd);
	  }
	}
      });

    mpat_sub_browse.add(mpat_sub_system);
    
    //create the menu item of the menu Pattern (User)
    JMenuItem mip_user = new JMenuItem("User Defined Patterns");
    mip_user.setMnemonic(KeyEvent.VK_U);
    mpat_sub_browse.add(mip_user);
    
    mip_user.setToolTipText("User Defined Pattern");
    mip_user.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if(currentfile.equals("")){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 1);
	    //dlg.show();
	  }   
	  else{	
	    MySelectAction dlg = new MySelectAction(1);
	    dlg.setTitle("Select Pattern");
	    dlg.setSize(new Dimension(500, 300));
	    dlg.show();
	    if(dlg.dlg.getFunc()!=""){
	      cmd_ind = "browse " + dlg.dlg.getFunc() + " \""+ currentcmp+"/"+currentfile + "\"\n";  
	      cmdLine.runHaskellCmd(cmd_ind);
	    }
	  }
	}
      });

    mPattern.add(mpat_sub_browse);

    //ceate the fifth menu (Indicator Data)
    JMenu mData = new JMenu("Data");
    mData.setMnemonic(KeyEvent.VK_D);
    mData.getAccessibleContext().setAccessibleDescription("Data Related");
    menuBar.add(mData);

    //a group of JMenuItems
    /**************************************
        Save to New Data File
     **************************************/
    JMenuItem mid_saveas = new JMenuItem("Save as ...");
    mid_saveas.setMnemonic(KeyEvent.VK_A);
    mData.add(mid_saveas);
    
    mid_saveas.setToolTipText("Save Indicator Data to a New File");
    mid_saveas.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if(graph.hasInd()==false){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 0);
	  }
	  else{
	    int IndicatorNo = graph.getIndicatorNo();
	    MyIndSaver dlg = new MyIndSaver(currentpath, IndicatorNo, graph.getIndNames());		    
	    dlg.setSize(400, 300);
	    dlg.setTitle("Save Indicator Data");
	    dlg.show();
	    if(dlg.actived){
	      int[] choice = dlg.getChoice();
	      String[] ind_names = dlg.getIndNames();
	      String fileDir = dlg.getFiledir();
	      //String formDir = dlg.getFordir();
	      int[] enddays = graph.getEndDay();
	      int[] startdays = graph.getStartDay();
	      //String[] funcs = graph.getIndFuncs();
	      MyDate[] dates = graph.getDate();
	      float[][] ti_datas = graph.getTI();
	      
	      int min_start = -1;
	      int max_end = -1;
	      
	      for(int i = 0; i<IndicatorNo; i++){
		if(choice[i] == 1){
		  if(min_start>startdays[i] || min_start<0)
		    min_start = startdays[i];
		  if(max_end<enddays[i] || max_end<0)
		    max_end = enddays[i];
		}
	      }
	      try{
		BufferedWriter fileout = new BufferedWriter(new FileWriter(fileDir));
	        BufferedWriter fileout_for = new BufferedWriter(new FileWriter(fileDir+".for"));
		String temp = new String();
		//String temp_f = new String();

		temp = temp.concat("date\t");
		for(int i = 0; i< IndicatorNo; i++){
		  if(choice[i] == 1){
		    temp = temp.concat(ind_names[i] + "\t");
		    //temp_f = temp_f.concat(ind_names[i] + " : " + funcs[i] + "\n");
		  }
		}
		fileout.write(temp.trim());
		fileout.newLine();
		for(int i = 0; i<max_end; i++){
		  temp = new String();
		  temp = temp.concat(dates[i]+ "\t");
		  for(int j = 0; j< IndicatorNo; j++){
		    if(choice[j] == 1){
			temp = temp.concat(ti_datas[j][i]+"\t");
		    }
		  }
		  fileout.write(temp.trim());
		  fileout.newLine();
		}
		//fileout_for.write(temp_f.trim());
		//fileout_for.flush();
		//fileout_for.close();
		fileout.flush();
		fileout.close();
		
		Frame parent = new Frame("Parent");
		AlertDlg savenotice = new AlertDlg(parent, true, 6);
		graph.ind_names = ind_names;
		graph.repaint();
	      }
	      catch (Exception er){
		System.err.println("Write error: "+er.getMessage());
	      }
	    }
	    
	  }
	}
      });

    /**************************************
        Save with Exist File (combine)
     **************************************/
    JMenuItem mid_savewith = new JMenuItem();
    mid_savewith.setText("Save with Original Data as ...");
    mid_savewith.setMnemonic(KeyEvent.VK_W);
    mData.add(mid_savewith);
    
    mid_savewith.setToolTipText("Save Indicator Data with Original Data to a New File");
    mid_savewith.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if(graph.hasInd()==false){
	    Frame parent = new Frame("Parent");
	    AlertDlg dlg = new AlertDlg(parent, true, 0);
	  }
	  else{
	    int IndicatorNo = graph.getIndicatorNo();
	    MyIndSaver dlg = new MyIndSaver(currentpath, IndicatorNo, graph.getIndNames());		    
	    dlg.setSize(400, 300);
	    dlg.setTitle("Save Indicator Data");
	    dlg.show();
	    if(dlg.actived){
	      int[] choice = dlg.getChoice();
	      String[] ind_names = dlg.getIndNames();
	      String fileDir = dlg.getFiledir();
	      //String formDir = dlg.getFordir();
	      int[] enddays = graph.getEndDay();
	      int[] startdays = graph.getStartDay();
	      float[][] ti_datas = graph.getTI();
	      
	      int min_start = -1;
	      int max_end = -1;
	      
	      //here may improve... in fact the startdays are all 0 
	      //and the enddays are all the size of the array
	      for(int i = 0; i<IndicatorNo; i++){
		if(choice[i] == 1){
		  if(min_start>startdays[i] || min_start<0)
		    min_start = startdays[i];
		  if(max_end<enddays[i] || max_end<0)
		    max_end = enddays[i];
		}
	      }
	      
	      try{
		BufferedReader filein  = new BufferedReader(new FileReader(currentpath));
		BufferedWriter fileout = new BufferedWriter(new FileWriter(fileDir));
		String temp = new String();
		String original_s = filein.readLine();
		StringTokenizer st = new StringTokenizer(original_s);
		while(st.hasMoreTokens())
		  temp = temp.concat(st.nextToken() + "\t");
		for(int i = 0; i< IndicatorNo; i++){
		  if(choice[i] == 1){
		    temp = temp.concat(ind_names[i] + "\t");
		  }
		}
		fileout.write(temp.trim());
		fileout.newLine();
		for(int i = 0; i<max_end; i++){
		  original_s = new String();
		  temp = new String();
		  original_s = filein.readLine();
		  st = new StringTokenizer(original_s);
		  while(st.hasMoreTokens())
		    temp = temp.concat(st.nextToken() + "\t");
		  for(int j = 0; j< IndicatorNo; j++){
		    if(choice[j] == 1){
			temp = temp.concat(ti_datas[j][i]+"\t");
		    }
		  }
		  fileout.write(temp.trim());
		  fileout.newLine();
		}
		filein.close();
		fileout.flush();
		fileout.close();
		
		Frame parent = new Frame("Parent");
		AlertDlg savenotice = new AlertDlg(parent, true, 6);
		graph.ind_names = ind_names;
		graph.repaint();
	      }
	      catch (Exception er){
		System.err.println("Write error: "+er.getMessage());
	      }
	    }	    
	  }
	}
      });
    
    /**************************************
        Combine 2 Files (Base on Date)
     **************************************/
    JMenuItem mid_combine = new JMenuItem("Combine ...");
    mid_combine.setMnemonic(KeyEvent.VK_C);
    mData.add(mid_combine);
    
    mid_combine.setToolTipText("combine 2 Data Files");
    mid_combine.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e) {
	  MyCombineDlg dlg = new MyCombineDlg();
	  dlg.setSize(new Dimension(600, 250));
	  dlg.setTitle("Select Files");
	  dlg.show();
	}
      });

    /**************************************
        Load Indicator Data
     **************************************/
    JMenuItem mid_load = new JMenuItem("Load ...");
    mid_load.setMnemonic(KeyEvent.VK_L);
    mData.add(mid_load);
    
    mid_load.setToolTipText("Load Formula");
    mid_load.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e) {
	  JFileChooser chooser = new JFileChooser(".//data//ascii");
	  chooser.addChoosableFileFilter(new IndDataFilter());
	  chooser.setAcceptAllFileFilterUsed(false);
	  int returnVal = chooser.showOpenDialog(pane);
	  if(returnVal == JFileChooser.APPROVE_OPTION) {
	    try{	
	      File file = chooser.getSelectedFile();
	      graph.loadIndicator(file.getAbsolutePath());
	    }
	    catch (Exception er){
	      System.err.println("Write error: "+er.getMessage());
	    }		    
	  }
	}
      });
    
    /******************************
              View Data
     ******************************/
    JMenuItem mid_view = new JMenuItem("View ...");
    mid_view.setMnemonic(KeyEvent.VK_V);
    mData.add(mid_view);

    mid_view.setToolTipText("About The System");
    mid_view.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  JFileChooser chooser = new JFileChooser(".//data//ascii");
	  chooser.addChoosableFileFilter(new DataFilter());
	  chooser.addChoosableFileFilter(new IndDataFilter());
	  chooser.setAcceptAllFileFilterUsed(false);
	  
	  int returnVal = chooser.showOpenDialog(pane);
	  if(returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = chooser.getSelectedFile();
	    MyTable table = new MyTable(file);
	    table.setTitle(file.getName());
	    table.setSize(700, 500);
	    table.show();
	  }
	}
      });

    //ceate the sixth menu (Tools)
    JMenu mSystem = new JMenu("System");
    mSystem.setMnemonic(KeyEvent.VK_S);
    mSystem.getAccessibleContext().setAccessibleDescription("System Setting Tools");
    menuBar.add(mSystem);
    
    JMenuItem mis_options = new JMenuItem("Options");
    mis_options.setMnemonic(KeyEvent.VK_O);
    mSystem.add(mis_options);
    
    mis_options.setToolTipText("System Options");
    mis_options.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  OptionDlg dlg = new OptionDlg();
	  dlg.setTitle("System Setting");
	  dlg.setSize(new Dimension(500, 600));
	  dlg.show();
	  if(dlg.actived){
	    AlertDlg recompile = new AlertDlg(parent, true, 10);     
	    try{
	      child = Runtime.getRuntime().exec("CPL.bat");
	    }catch(IOException runerror){
	      System.err.println("Test!");
	    }
	    dispose();
	  }
	}
      });

    JMenuItem mis_editor = new JMenuItem("Editor");
    mis_editor.setMnemonic(KeyEvent.VK_E);
    mSystem.add(mis_editor);
    
    mis_editor.setToolTipText("Run Default Editor");
    mis_editor.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  try{
	    child = Runtime.getRuntime().exec("D:\\emacs-21.3\\bin\\runemacs.exe");
	  }catch(IOException runerror){
	    System.err.println("Test!");
	  }
	}
      });
    
    JMenuItem mis_reboot_c = new JMenuItem("Reboot CPL");
    mSystem.add(mis_reboot_c);
    
    mis_reboot_c.setToolTipText("Reboot System");
    mis_reboot_c.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  try{
	    child = Runtime.getRuntime().exec("CPL.bat");
	  }catch(IOException runerror){
	    System.err.println("Test!");
	  }
	  dispose();
	}
      });
    
    JMenuItem mis_reboot_h = new JMenuItem("Reboot Haskell");
    mSystem.add(mis_reboot_h);    
    mis_reboot_h.setToolTipText("Reboot Haskell");
    mis_reboot_h.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  cmdLine.runHaskellCmd(":r\n");
	}
      });

    //ceate the seventh menu (Help)
    JMenu mHelp = new JMenu("Help");
    mHelp.setMnemonic(KeyEvent.VK_H);
    mHelp.getAccessibleContext().setAccessibleDescription("System Related");
    menuBar.add(mHelp);
    
    JMenuItem mih_about = new JMenuItem("About");
    mih_about.setMnemonic(KeyEvent.VK_A);
    mHelp.add(mih_about);
    
    mih_about.setToolTipText("About The System");
    mih_about.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  AlertDlg dlg = new AlertDlg(parent, true, 4);
	}
      });
    
    

    pane.add(menuBar, BorderLayout.NORTH);
    pane.add(graph, BorderLayout.CENTER);
    pane.add(cmdLine,BorderLayout.SOUTH);
    pack();
    setVisible(true);
    
    MyListener myListener = new MyListener();
    graph.addMouseListener(myListener);
    graph.addMouseMotionListener(myListener);
    
  }
  class MyListener extends MouseInputAdapter {
    /*
      public void mouseMoved(MouseEvent e) {
      saySomething("Mouse moved", e);
      }
      
      public void mouseDragged(MouseEvent e) {
      saySomething("Mouse dragged", e);
      }
      
      public void mousePressed(MouseEvent e) {
      saySomething("Mouse pressed", e);
      } 
      
      public void mouseEntered(MouseEvent e) {
      saySomething("Mouse entered", e);
      } 
      
      void saySomething(String eventDescription, MouseEvent e) {
      cmdLine.append(eventDescription 
      + " (" + e.getX() + "," + e.getY() + ")"
      + " detected on "
      + e.getComponent().getClass().getName()
      + "\n");
      //cmdLine.setCaretPosition(cmdLine.getDocument().getLength());
      }
    */
  }
  
  public void combineIndView(float[][]ti_datas, MyDate[] ti_date, String[] ti_names, 
			     int noBars, int[]choice, int IndicatorNo){
    IndWin indwin = new IndWin(this, ti_datas, ti_date, ti_names, noBars, choice, IndicatorNo, currentpath);
    indwin.setSize(new Dimension(608, 462));
    //indwin.setSize(new Dimension(630, 480));
    indwin.setTitle(currentfile +" - Technical Indicator ");
  }
  public void actOnDirective(String result){
    if(result.equals("Failed")){
      Frame parent = new Frame("Parent");
      AlertDlg dlg = new AlertDlg(parent, true, 9);
      if(dlg.action_a){
	try{
	  BufferedReader stdin_cpl = new BufferedReader(new FileReader(".\\CPL.hs"));
	  String cpl_content = "";
	  String temp = stdin_cpl.readLine();
	  boolean cpl_ok = false;
	  while(temp != null){
	    if(!cpl_ok){
	      if(temp.equals("--user created start--")){
		cpl_content = cpl_content + temp + "\n";
		temp = stdin_cpl.readLine();
		if(temp.equals("--user created end--")){
		  cpl_content = cpl_content + temp + "\n";
		}
		cpl_ok = true;
	      }
	      
	      else{
		cpl_content = cpl_content + temp + "\n";
	      }
	    }
	    else{
	      cpl_content = cpl_content + temp + "\n";
	    }
	    temp = stdin_cpl.readLine();
	  }  
	  stdin_cpl.close();
	  
	  BufferedWriter stdout_cpl = new BufferedWriter(new FileWriter(".\\CPL.hs"));
	  stdout_cpl.write(cpl_content.trim());
	  stdout_cpl.flush();
	  stdout_cpl.close();
	}
	catch (Exception er){
	  System.err.println("Write error: "+er.getMessage());
	}
	cmdLine.runHaskellCmd(":r\n");
      }
      else{}
    }
    else{
      StringTokenizer st = new StringTokenizer(result);
      String dir = st.nextToken().substring(11);//11 = length("_DIRECTIVE_")
      if(dir.equals("BROWSE")){
	if (st.hasMoreTokens()){  
	  Browser browser = new Browser(this,st.nextToken());
	}
	else cmdLine.append("No pattern instances found\n");
      }
      
      else if(dir.equals("TI")){
	if (st.hasMoreTokens()){
	  Techind tech = new Techind(this,st.nextToken());
	  int noInd = graph.IndicatorNo;
	  int noBars = graph.noOfPoints;
	  String ti_name = (graph.getIndNames())[noInd-1];
	  float[] ti_data = (graph.getTI())[noInd-1];
	  MyDate[] ti_date = graph.getDate();
	  IndWin indwin = new IndWin(this, ti_data, ti_date, noBars, ti_name, currentpath);
	  indwin.setSize(new Dimension(608, 462));
	  indwin.setTitle(currentfile +" - Technical Indicator " + graph.IndicatorNo);
	}
	else cmdLine.append("No Indicator instances found\n");
      }
      
      else{
	System.out.println("What is that directive? " + dir);
      }
    }
  }
  
  public static void main (String args[]){
    try {
      UIManager.setLookAndFeel(
			       UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) { }
    CPL cpl = new CPL();
  }
}

/********************************
save indicator calculate result
 ********************************/

class MyIndSaver extends JDialog{
  int choice[] = {0, 0, 0, 0, 0};
  String[] ind_names = new String[5];
  int IndicatorNo = 0;
  String currentpath;
  String sFileName = "";
  String sCurrFile = ""; 
  String sCurrDir = "";
  //String formulaDir = "";
  boolean actived = false;
  DialogFrame dlg;
  boolean viewgraph = false;
      
  public MyIndSaver(String currentpath, int IndicatorNo, String[] ind_names){
    this.currentpath = currentpath;
    this.IndicatorNo = IndicatorNo;
    this.ind_names = ind_names;
    dlg = new DialogFrame();
    setModal(true);
    getContentPane().add(BorderLayout.CENTER,dlg);
    pack();	
  }
  public MyIndSaver(String currentpath, int IndicatorNo, String[] ind_names, String flag){
    this.currentpath = currentpath;
    this.IndicatorNo = IndicatorNo;
    this.ind_names = ind_names;
    viewgraph = true;
    dlg = new DialogFrame();
    setModal(true);
    getContentPane().add(BorderLayout.CENTER,dlg);
    pack();	
  }
  
  public String getFiledir(){
    return sCurrDir;
  }
  /*
  public String getFordir(){
    return formulaDir;
  }
  */
  public int[] getChoice(){
    return choice;
  }
  
  public String[] getIndNames(){
    return ind_names;
  }
  
  class DialogFrame extends JPanel{
    JCheckBox[] ti_c = new JCheckBox[IndicatorNo];
    //JCheckBox ti_c5 = new JCheckBox(ind_names[4]);
   
    // JTextField ti_t1 = new JTextField(ind_names[0]);
    JTextField[] ti_t = new JTextField[IndicatorNo];

    Frame parent = new Frame("Parent");
    boolean overwrite = true;

    private void addLabelTextRows(JCheckBox[] checkboxs,
				  JTextField[] textFields,
				  GridBagLayout gridbag,
				  Container container) {
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.EAST;
      int numCheckBoxs = checkboxs.length;
      
      for (int i = 0; i < numCheckBoxs; i++) {
	c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
	c.fill = GridBagConstraints.NONE;      //reset to default
	c.weightx = 0.0;                       //reset to default
	container.add(checkboxs[i], c);
	
	c.gridwidth = GridBagConstraints.REMAINDER;     //end row
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	container.add(textFields[i], c);
      }
    }
    
    public DialogFrame(){
      try {
	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception e) { }
      
      for(int i = 0; i<IndicatorNo; i++){
	ti_c[i] = new JCheckBox("Indicator " +(i+1)+ ":  ");
      }
      for(int i = 0; i<IndicatorNo; i++){
	ti_t[i] = new JTextField(ind_names[i], 20);
      }

      JButton view = new JButton("View");
      view.setToolTipText("Save New Indicator Result");
      view.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){	
	    for(int i = 0; i < IndicatorNo; i++){
	      if(ti_c[i].isSelected()){
		choice[i] = 1;
		if(ti_t[i].getText()!=null)
		  ind_names[i] = ti_t[i].getText();
		else{}
	      }
	    }
	    actived = true;
	    dispose();
	  }
	});

      JButton save = new JButton("Save");
      save.setToolTipText("Save New Indicator Result");
      save.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){	
	    JFileChooser saver = new JFileChooser(currentpath);
	    saver.addChoosableFileFilter(new IndDataFilter());
	    saver.setAcceptAllFileFilterUsed(false);
	    int returnVal = saver.showSaveDialog(getContentPane());
	    if(returnVal == JFileChooser.APPROVE_OPTION){
	      File file = saver.getSelectedFile();
	      if(file.exists()){
		AlertDlg ad = new AlertDlg(parent, true, 3);
		if(ad.action_a)
		  overwrite = true;
		else
		  overwrite = false;
	      }
	      if(overwrite){
		sFileName = file.getName();
		sCurrDir = file.getAbsolutePath();
		
		StringTokenizer st = new StringTokenizer(sCurrDir, ".");
		String tmp = "";
		while(st.hasMoreTokens())
		  tmp = st.nextToken();
		if(!tmp.equals("ind")){
		  sFileName = sFileName + ".ind";
		  sCurrDir = sCurrDir + ".ind";
		}
		//formulaDir = sCurrDir + ".for";
		
		for(int i = 0; i < IndicatorNo; i++){
		  if(ti_c[i].isSelected()){
		    choice[i] = 1;
		    if(ti_t[i].getText()!=null)
		      ind_names[i] = ti_t[i].getText();
		    else{}
		  }
		}
		actived = true;
		dispose();
	      }
	      else{}
	    }
	  }
	});
      
      JButton cancel = new JButton("Cancel");
      cancel.setToolTipText("Cancel");
      cancel.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    actived = false;
	    dispose();
	  }
	});
      
      JPanel textControlsPane = new JPanel();
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      
      textControlsPane.setLayout(gridbag);
      
      JCheckBox[] checkboxs = new JCheckBox[IndicatorNo];
      for(int i = 0; i < IndicatorNo; i++)
	checkboxs[i] = ti_c[i];
      
      JTextField[] textFields = new JTextField[IndicatorNo];
      for(int i = 0; i < IndicatorNo; i++)
	textFields[i] = ti_t[i];

      addLabelTextRows(checkboxs, textFields, gridbag, textControlsPane);

      c.gridwidth = GridBagConstraints.REMAINDER; //last
      c.anchor = GridBagConstraints.WEST;
      c.weightx = 1.0;
      textControlsPane.setBorder(
				 BorderFactory.createCompoundBorder(
				 BorderFactory.createTitledBorder("Save Indicator"),
                                 BorderFactory.createEmptyBorder(5,5,5,5)));

      JPanel leftPane = new JPanel(new GridLayout(1,0));
      leftPane.add(textControlsPane);

      JPanel rightPane = new JPanel(new BorderLayout());
      if(viewgraph)
	rightPane.add(view,BorderLayout.LINE_START);
      else
	rightPane.add(save,BorderLayout.LINE_START);
      rightPane.add(cancel,BorderLayout.LINE_END);

      add(leftPane, BorderLayout.PAGE_START);
      add(rightPane, BorderLayout.PAGE_END);
    }
  }
}

/***************************************
The Dialog let the user create new 
indicator function definition
 **************************************/

class MyEditor extends JDialog{
  File f = null;
  boolean editst_f = true;
  boolean editst_d = true;
  String importFunc = "";
  String iorp = "";
  DialogFrame dlg;
  
  public MyEditor(String iorp){
    setTitle("New File");
    this.iorp = iorp;
    dlg = new DialogFrame();
    setModal(true);
    getContentPane().add(BorderLayout.CENTER,dlg);
    pack();	
  }
  
  public MyEditor(File f, String viewst, String iorp){
    this.f = f;
    this.iorp = iorp;
    if(viewst.equals("View")){
      editst_f = false;
      editst_d = false;
    }
    else{
      editst_f = false;
      editst_d = true;
    }
    setTitle(f.getName());
    dlg = new DialogFrame();
    setModal(true);
    getContentPane().add(BorderLayout.CENTER,dlg);
    pack();
  }
  
  class DialogFrame extends JPanel{
    String sFileName = "";
    String sFunc = "";
    String sCurrDir = "";
    String sRecordDir = "";
    boolean save_file = false;
    boolean overwrite = false;
    
    JLabel name_l = new JLabel(iorp + " Function:");
    JLabel define_l = new JLabel(iorp + " Definition:");
    JTextArea define_t = new JTextArea();
    JTextField name_t = new JTextField(50);

    public DialogFrame(){
      if(f != null){
	String sFunc = f.getName();
	sFunc = sFunc.substring(0, sFunc.length()-3);
	char[] lower = sFunc.toCharArray();
	lower[0] = Character.toLowerCase(lower[0]);
	sFunc = lower[0] + sFunc.substring(1);
	name_t.setText(sFunc);
	name_t.setEditable(editst_f);
	define_t.setEditable(editst_d);

	try{
	  BufferedReader stdin = new BufferedReader(new FileReader(f.getAbsolutePath()));
	  String s = stdin.readLine();
	  s = stdin.readLine();
	  boolean check_h = false;

	  while(s != null){
	    if(check_h){
	      define_t.append(s+ "\n");
	    }
	    else{
	      if(s.indexOf("--default import end--")>=0){
		check_h = true;
	      }
	    }
	    s = stdin.readLine();
	  }
	  stdin.close();
	}
	catch (Exception er){
	  System.err.println("Write error: "+er.getMessage());
	}
      }

      JButton save = new JButton("Save");
      save.setToolTipText("Save " + iorp);
      save.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    // Save File
	    Frame parent = new Frame("Parent");
	    sFunc = name_t.getText();
	    char[] upper = sFunc.toCharArray();
	    if(upper.length <= 0){
	      AlertDlg ad = new AlertDlg(parent, true, 7);
	    }
	    else{
	      
	      upper[0] = Character.toUpperCase(upper[0]);
	      sFileName = upper[0] + sFunc.substring(1) + ".hs";
	      upper[0] = Character.toLowerCase(upper[0]);
	      sFunc = upper[0] + sFunc.substring(1);

	      if(iorp.equals("Indicator")){
		sCurrDir = ".//Indicators//"+sFileName;
		sRecordDir = ".//Indicators//IndRecord";
	      }
	      else{
		sCurrDir = ".//Patterns//"+sFileName;
		sRecordDir = ".//Patterns//PatRecord";
	      }
	      Vector record_t = new Vector();
	      boolean flag = false;
	      
	      try{
		BufferedReader stdin = new BufferedReader(new FileReader(sRecordDir));
		String s = stdin.readLine();
		while(s != null){
		  record_t.add(s);
		  s = stdin.readLine();
		}
		stdin.close();
	      }
	      catch (Exception er){
		System.err.println("Write error: "+er.getMessage());
	      }
 	    
	      if(record_t.contains(sFunc)){
		AlertDlg dlg = new AlertDlg(parent, true, 3);
		save_file = dlg.action_a;
		if(save_file)
		  overwrite = true;
		else
		  overwrite = false;
	      }
	      else{
		save_file = true;
		for(int j = 0; j<record_t.size(); j++){
		  if(!flag && sFunc.compareTo(record_t.elementAt(j))<0){
		    record_t.insertElementAt(sFunc, j);
		    flag = true;
		    break;
		  }
		}
		if(!flag)
		  record_t.addElement(sFunc);	      
	      }
	      
	      if(save_file){
		try{
		  BufferedWriter stdout = new BufferedWriter(new FileWriter(sCurrDir));
		  String header = "module " + sFileName.substring(0, sFileName.length()-3) + " where\n";
		  header = header + "--default import start--\nimport IOExts\nimport CPLTI"+
		    "\nimport CPLPattern"+
		    "\nimport Prelude hiding ((>),(==),(<),(<=),(>=),(&&),(||))"+
		    "\nimport BuiltinPatterns\n--default import end--";
		  stdout.write(header.trim());
		  stdout.newLine();
		  stdout.newLine();
		  stdout.write(getData().trim());
		  stdout.flush();
		  stdout.close();
		  
		  if(!overwrite){
		    BufferedWriter stdout_r = new BufferedWriter(new FileWriter(sRecordDir));
		    for(int i = 0; i<record_t.size(); i++){
		      stdout_r.write(((String)record_t.elementAt(i)).trim());
		      stdout_r.newLine();
		    }
		    stdout_r.flush();
		    stdout_r.close();
		    
		    BufferedReader stdin_cpl = new BufferedReader(new FileReader(".\\CPL.hs"));
		    String cpl_content = "";
		    String temp = stdin_cpl.readLine();
		    boolean cpl_addcheck = false;
		    String importFunc = "import " + sFileName.substring(0, sFileName.length()-3);
		    
		    while(temp != null){
		      cpl_content = cpl_content + temp + "\n";
		      if(!cpl_addcheck){
			if(temp.indexOf("--user created start--") == 0){
			  cpl_content = cpl_content + importFunc + "\n";
			}
		      }
		      temp = stdin_cpl.readLine();
		    }  
		    stdin_cpl.close();
		    
		    BufferedWriter stdout_cpl = new BufferedWriter(new FileWriter(".\\CPL.hs"));
		    stdout_cpl.write(cpl_content.trim());
		    stdout_cpl.flush();
		    stdout_cpl.close();
		  }
		  else{
		    BufferedReader stdin_cpl = new BufferedReader(new FileReader(".\\CPL.hs"));
		    String cpl_content = "";
		    String temp = stdin_cpl.readLine();
		    boolean cpl_addcheck = false;
		    String importFunc = "import " + sFileName.substring(0, sFileName.length()-3);
		    Vector forCheck = new Vector();
		    
		    while(temp != null){
		      if(!cpl_addcheck){
			cpl_content = cpl_content + temp + "\n";
			if(temp.indexOf("--user created start--") == 0){
			  cpl_addcheck = true;
			}
		      }
		      else{
			if(temp.indexOf("--user created end--") == 0){
			  if(forCheck.contains(importFunc)){
			    forCheck.removeElement(importFunc);
			  }
			  cpl_content = cpl_content + importFunc + "\n";
			  for(int i = 0; i < forCheck.size(); i++){
			    cpl_content = cpl_content + forCheck.elementAt(i)+ "\n";
			  }
			  cpl_content = cpl_content + temp + "\n";
			  cpl_addcheck = false;
			}
			else{
			  forCheck.addElement(temp);
			}
		      }
		      temp = stdin_cpl.readLine();  
		    }
		    stdin_cpl.close();
		    
		    BufferedWriter stdout_cpl = new BufferedWriter(new FileWriter(".\\CPL.hs"));
		    stdout_cpl.write(cpl_content.trim());
		    stdout_cpl.flush();
		    stdout_cpl.close();
		  }
		  dispose();
		}
		catch (Exception er){
		  System.err.println("Write error: "+er.getMessage());
		}	     
	      } 
	    }
	  }
	});
      
      JButton cancel = new JButton("Cancel");
      cancel.setToolTipText("Cancel");
      cancel.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    dispose();
	  }
	});
      
      JPanel panel1 = new JPanel();
      panel1.add(name_t);
      panel1.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Function Name: "),
                                BorderFactory.createEmptyBorder(3,3,3,3)));
      
      JScrollPane scrollPane = new JScrollPane(define_t,
					       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setPreferredSize(new Dimension(580, 450));
      scrollPane.setBorder(
	    BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Definition: "),
                                BorderFactory.createEmptyBorder(3,3,3,3)),
                                scrollPane.getBorder()));
      /*
      JPanel panel2 = new JPanel();
      panel2.add(scrollPane);
      panel2.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Definition: "),
                                BorderFactory.createEmptyBorder(3,3,3,3)));
      */

      JPanel panel3 = new JPanel(new BorderLayout());
      panel3.add(save, BorderLayout.LINE_START);
      panel3.add(cancel, BorderLayout.LINE_END);

      add(panel1, BorderLayout.PAGE_START);
      add(scrollPane, BorderLayout.CENTER);
      add(panel3, BorderLayout.PAGE_END);
    }
    public String getData(){
      return define_t.getText();
    }
    public String getFileName(){
      return sFileName;
    }
    public boolean getChangeState(){
      return save_file;
    }
  }
}


/********************************************
The dialog let user choose the basic parameter
for Indicator Calculation
*******************************************/
class MyMovingAvg extends JDialog{
  DialogFrame dlg = new DialogFrame();
  Frame parent;
  public MyMovingAvg(Frame parent){	    	
    setTitle("MovingAvg");
    this.parent = parent;
    setModal(true);
    getContentPane().add(BorderLayout.CENTER,dlg);
    pack();	
  }
  
  class DialogFrame extends JPanel{
    JLabel basedays_l = new JLabel("Base on how many days:");
    JLabel basevalue_l = new JLabel("Base on which value:");
    JLabel days = new JLabel("Days");

    JTextField basedays_t = new JTextField(20);
    //JComboBox basevalue_c = new JComboBox();
    JTextField basevalue_t = new JTextField(50);

    String basedays_s = "";
    String basevalue_s = "";
    
    boolean thereIsInd = false;
    
    public DialogFrame(){
      try {
	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception e) { }
      
      //basevalue_c.addItem("close");
      //basevalue_c.addItem("open");
      //basevalue_c.addItem("high");
      //basevalue_c.addItem("low");
      
      JButton ok_b = new JButton("OK");
      JButton cancel_b = new JButton("Cancel");
      
      JPanel pan1 = new JPanel(new BorderLayout());
      pan1.add(basedays_l, BorderLayout.PAGE_START);
      pan1.add(basedays_t, BorderLayout.PAGE_END);
      
      /*
      JPanel panel_p2 = new JPanel();
      //panel_p2.setLayout(new FlowLayout());
      panel_p2.setLayout(new BoxLayout(panel_p2, BoxLayout.X_AXIS));
      panel_p2.add(basedays_l);
      panel_p2.add(basedays_t);
      panel_p2.add(days);
      */

      JPanel pan2 = new JPanel(new BorderLayout());
      pan2.add(basevalue_l, BorderLayout.PAGE_START);
      pan2.add(basevalue_t, BorderLayout.PAGE_END);
      
      /*
      JPanel panel_p3 = new JPanel();
      //panel_p3.setLayout(new FlowLayout());
      panel_p3.setLayout(new BoxLayout(panel_p3, BoxLayout.X_AXIS));
      panel_p3.add(basevalue_l);
      //panel_p3.add(basevalue_c);
      panel_p3.add(basevalue_t);
      */
      
      JPanel pan3 = new JPanel(new BorderLayout());
      pan3.add(ok_b, BorderLayout.LINE_START);
      pan3.add(cancel_b, BorderLayout.LINE_END);
      /*
      JPanel panel_p4 = new JPanel();
      panel_p4.setLayout(new FlowLayout());
      //panel_p4.setLayout(new BoxLayout(panel_p4, BoxLayout.X_AXIS));
      panel_p4.add(ok_b);
      panel_p4.add(cancel_b);
      */
      
      JPanel pan_up = new JPanel(new BorderLayout());
      pan_up.add(pan1, BorderLayout.PAGE_START);
      pan_up.add(pan2, BorderLayout.PAGE_END);
      pan_up.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Parameter: "),
                                BorderFactory.createEmptyBorder(10,10,10,10)));

      
      ok_b.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    basedays_s = basedays_t.getText();
	    if(basedays_s.equals("")){
	      AlertDlg dlg = new AlertDlg(parent, true, 5);
	    }
	    else{
	      //basevalue_s = (String)basevalue_c.getSelectedItem();
	      basevalue_s = basevalue_t.getText();
	      thereIsInd = true;
	      dispose();
	    }
	  }
	});
      
      cancel_b.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    thereIsInd = false;
	    dispose();
	  }
	});
      
      add(pan_up, BorderLayout.PAGE_START);
      add(pan3, BorderLayout.PAGE_END);
    }
  }
}

class MySelect extends JDialog{
  String button_word = "";
  String iorp = "";
  String importFunc = "";
  DialogFrame dlg;
  public MySelect(String button_word, String iorp){
    this.button_word = button_word;
    this.iorp = iorp;
    dlg = new DialogFrame();
    setTitle("Select Indicator");
    setModal(true);
    getContentPane().add(BorderLayout.CENTER,dlg);
    pack();	
  }
  
  class DialogFrame extends JPanel{
    JLabel name_l = new JLabel("Select " + iorp +" Name:");
    JComboBox name_c = new JComboBox();
    String name_s = "";
    boolean changed = false;
    Vector fun_r = new Vector();
    String sRecordDir = "";    
    String selectedFun = "";
    String selectedFile = "";
    
    public DialogFrame(){
      try {
	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception e) { }
      if(iorp.equals("Indicator"))
	sRecordDir = ".\\Indicators\\IndRecord";
      else
	sRecordDir = ".\\Patterns\\PatRecord";
      String path = sRecordDir;
      name_c.setSize(new Dimension(200, 100));

      try{
	BufferedReader stdin = new BufferedReader(new FileReader(path));
	String s = stdin.readLine();
	while(s != null){
	  name_c.addItem(s);
	  fun_r.addElement(s);
	  s = stdin.readLine();
	}
	stdin.close();
      }
      catch (Exception er){
	System.err.println("Write error: "+er.getMessage());
      }
      
      JButton var_b = new JButton(button_word);
      JButton cancel_b = new JButton("Cancel");
      
      JPanel panel_p0 = new JPanel(new BorderLayout());
      panel_p0.add(name_c, BorderLayout.CENTER);
      panel_p0.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Select: "),
                                BorderFactory.createEmptyBorder(10,10,10,10)));

      JPanel panel_p1 = new JPanel(new BorderLayout());
      panel_p1.add(var_b, BorderLayout.LINE_START);
      panel_p1.add(cancel_b, BorderLayout.LINE_END);
      
      var_b.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    selectedFun = (String)name_c.getSelectedItem();
	    char[] upper = selectedFun.toCharArray();
	    upper[0] = Character.toUpperCase(upper[0]);
	    selectedFile = upper[0] + selectedFun.substring(1) + ".hs";
	    File fl = new File(".\\" + iorp + "s\\" + selectedFile);	      
	    if(button_word.equals("Delete")){
	      fl.delete();
	      fun_r.remove(selectedFun);
	      try{
		BufferedWriter stdout_r = new BufferedWriter(new FileWriter(sRecordDir));
		for(int i = 0; i<fun_r.size(); i++){
		  stdout_r.write(((String)fun_r.elementAt(i)).trim());
		  stdout_r.newLine();
		}
		stdout_r.flush();
		stdout_r.close();
		
		BufferedReader stdin_cpl = new BufferedReader(new FileReader(".\\CPL.hs"));
		String cpl_content = "";
		String temp = stdin_cpl.readLine();
		boolean cpl_addcheck = false;
		while(temp != null){
		  if(temp.equals("import " + upper[0] + selectedFun.substring(1))){
		  }
		  else{
		    cpl_content = cpl_content + temp + "\n";
		  }
		  temp = stdin_cpl.readLine();
		}  
		stdin_cpl.close();
		
		BufferedWriter stdout_cpl = new BufferedWriter(new FileWriter(".\\CPL.hs"));
		stdout_cpl.write(cpl_content.trim());
		stdout_cpl.flush();
		stdout_cpl.close();
	      }
	      catch (Exception er){
		System.err.println("Write error: "+er.getMessage());
	      }
	      changed = true;
	    }
	    else if(button_word.equals("Modify")){
	      dispose();
	      MyEditor dlg = new MyEditor(fl, button_word, iorp);
	      dlg.setSize(600, 600);
	      dlg.setTitle("Modify " + fl.getName());
	      dlg.show();
	      changed = dlg.dlg.getChangeState();
	    }
	    else if(button_word.equals("View")){
	      dispose();
	      MyEditor dlg = new MyEditor(fl, button_word, iorp);
	      dlg.setSize(600, 600);
	      dlg.setTitle("View " + fl.getName());
	      dlg.show();
	    }
	    dispose();
	  }
	});
      
      cancel_b.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    changed = false;
	    dispose();
	  }
	});
      
      add(panel_p0, BorderLayout.PAGE_START);
      add(panel_p1, BorderLayout.PAGE_END);
    }
    public String getFileName(){
      if(changed)
	return selectedFile;
      else
	return "";
    }
  }
}

class MySelectAction extends JDialog{
  int iorp = -1;
  DialogFrame dlg;
  public MySelectAction(int iorp){
    if(iorp == 0)
      setTitle("Select Indicator");
    else
      setTitle("Select Pattern");
    this.iorp = iorp;

    dlg = new DialogFrame();
    setModal(true);
    getContentPane().add(BorderLayout.CENTER,dlg);
    pack();	
  }  
  class DialogFrame extends JPanel{
    JComboBox name_c = new JComboBox();
    JTextField para_t = new JTextField(40);
    JButton ok_b = new JButton("OK");
    JButton cancel_b = new JButton("Cancel");
    String selectedFun = "";

    public DialogFrame(){
      try {
	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception e) { }

      JPanel pan_up = new JPanel(new BorderLayout());
      if(iorp == 0){
	pan_up.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Please Select Indicator "),
                                BorderFactory.createEmptyBorder(10,10,10,10)));
	String path = ".\\Indicators\\IndRecord";
	try{
	  BufferedReader stdin = new BufferedReader(new FileReader(path));
	  String s = stdin.readLine();
	  while(s != null){
	    name_c.addItem(s);
	    s = stdin.readLine();
	  }
	  stdin.close();
	}
	catch (Exception er){
	  System.err.println("Write error: "+er.getMessage());
	}
      }
      else{
	pan_up.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Please Select Pattern "),
                                BorderFactory.createEmptyBorder(10,10,10,10)));

	String path = ".\\Patterns\\PatRecord";
	try{
	  BufferedReader stdin = new BufferedReader(new FileReader(path));
	  String s = stdin.readLine();
	  while(s != null){
	    name_c.addItem(s);
	    s = stdin.readLine();
	  }
	  stdin.close();
	}
	catch (Exception er){
	  System.err.println("Write error: "+er.getMessage());
	}
      }
      pan_up.add(name_c);
      
      JPanel pan_center = new JPanel(new BorderLayout());
      pan_center.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Other Parameters(IF ANY) "),
                                BorderFactory.createEmptyBorder(10,10,10,10)));

      pan_center.add(para_t);

      JPanel pan_down = new JPanel(new BorderLayout());
      pan_down.add(ok_b, BorderLayout.LINE_START);
      pan_down.add(cancel_b, BorderLayout.LINE_END);
      
      ok_b.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    selectedFun = "(" + (String)name_c.getSelectedItem() + " " + para_t.getText() + ")";
	    dispose();
	  }
	});
      
      cancel_b.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    dispose();
	  }
	});
      
      add(pan_up, BorderLayout.PAGE_START);
      add(pan_center, BorderLayout.CENTER);
      add(pan_down, BorderLayout.PAGE_END);
      
    }

    public String getFunc(){
	return selectedFun;
    }
  }
}

/*************************************
Dialog to choose 2 data file to combine
 *************************************/
class MyCombineDlg extends JDialog{
  DialogFrame dlg;
  public MyCombineDlg(){
    dlg = new DialogFrame();
    setModal(true);
    getContentPane().add(BorderLayout.CENTER,dlg);
    pack();	
  }  
  class DialogFrame extends JPanel{
    JTextField filename_t1 = new JTextField(40);
    JTextField filename_t2 = new JTextField(40);
    String display_1 = "";
    String display_2 = "";
    String filepath_1 = "";
    String filepath_2 = "";
    JButton browse_1 = new JButton("Browse");
    JButton browse_2 = new JButton("Browse");
    JButton combine_b = new JButton("Combine");
    JButton cancel_b = new JButton("Cancel");

    public DialogFrame(){
      try {
	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception e) { }
      
      JPanel pan_1 = new JPanel(new BorderLayout());
      pan_1.add(filename_t1, BorderLayout.LINE_START);
      pan_1.add(browse_1, BorderLayout.LINE_END);
      pan_1.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("File I "),
                                BorderFactory.createEmptyBorder(10,10,10,10)));

      JPanel pan_2 = new JPanel(new BorderLayout());
      pan_2.add(filename_t2, BorderLayout.LINE_START);
      pan_2.add(browse_2, BorderLayout.LINE_END);
      pan_2.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("File II "),
                                BorderFactory.createEmptyBorder(10,10,10,10)));
      
      JPanel pan_3 = new JPanel(new BorderLayout());
      pan_3.add(combine_b, BorderLayout.LINE_START);
      pan_3.add(cancel_b, BorderLayout.LINE_END);

      /*
      JLabel sf1 = new JLabel("File 1: ");
      sf1.setHorizontalTextPosition(JLabel.LEFT);
      JLabel sf2 = new JLabel("File 2: ");
      sf2.setHorizontalTextPosition(JLabel.LEFT);

      JPanel panel_p0 = new JPanel();
      panel_p0.setLayout(new BoxLayout(panel_p0, BoxLayout.X_AXIS));
      panel_p0.add(filename_t1);
      panel_p0.add(browse_1);

      JPanel panel_p1 = new JPanel();
      panel_p1.setLayout(new BoxLayout(panel_p1, BoxLayout.X_AXIS));
      panel_p1.add(filename_t2);
      panel_p1.add(browse_2);
		       
      JPanel panel_p2 = new JPanel();
      panel_p2.setLayout(new FlowLayout());
      panel_p2.add(combine_b);
      panel_p2.add(cancel_b);
      
      JPanel panel_p3 = new JPanel();
      panel_p3.setLayout(new GridLayout(2,1));
      panel_p3.add(sf1);
      panel_p3.add(panel_p0);
      
      JPanel panel_p4 = new JPanel();
      panel_p4.setLayout(new GridLayout(2,1));
      panel_p4.add(sf2);
      panel_p4.add(panel_p1);
      */
      browse_1.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    JFileChooser chooser = new JFileChooser(".//data//ascii");
	    chooser.addChoosableFileFilter(new DataFilter());
	    chooser.addChoosableFileFilter(new IndDataFilter());
	    chooser.setAcceptAllFileFilterUsed(false);
	  
	    int returnVal = chooser.showOpenDialog(getContentPane());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	      File file = chooser.getSelectedFile();
	      display_1 = file.getName();
	      filename_t1.setText(display_1);
	      filepath_1 = file.getAbsolutePath();
	    }
	  }
	});

      browse_2.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    JFileChooser chooser = new JFileChooser(".//data//ascii");
	    chooser.addChoosableFileFilter(new DataFilter());
	    chooser.addChoosableFileFilter(new IndDataFilter());
	    chooser.setAcceptAllFileFilterUsed(false);
	  
	    int returnVal = chooser.showOpenDialog(getContentPane());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	      File file = chooser.getSelectedFile();
	      display_2 = file.getName();
	      filename_t2.setText(display_2);
	      filepath_2 = file.getAbsolutePath();
	    }
	  }
	});

      combine_b.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    Frame parent = new Frame("Parent");
	    boolean overwrite = true;

	    try{
	      JFileChooser chooser = new JFileChooser(".//data//ascii");
	      chooser.addChoosableFileFilter(new IndDataFilter());
	      chooser.setAcceptAllFileFilterUsed(false);
	      int returnVal = chooser.showSaveDialog(getContentPane());
	      if(returnVal == JFileChooser.APPROVE_OPTION) {	      
		File file = chooser.getSelectedFile();
		String filepath = file.getAbsolutePath();
		StringTokenizer st = new StringTokenizer(filepath, ".");
		String tmp = "";
		while(st.hasMoreTokens())
		  tmp = st.nextToken();
		if(!tmp.equals("ind")){
		  filepath = filepath + ".ind";
		}
		file = new File(filepath);
		if(file.exists()){
		  AlertDlg savenotice = new AlertDlg(parent, true, 3);
		  boolean savef = savenotice.action_a;
		  if(savef)
		    overwrite = true;
		  else
		    overwrite = false;
		}
		if(overwrite){
		  

		  String content = new String();
		  BufferedReader filein1;
		  BufferedReader filein2;
		  File fl1_f = new File(filepath_1 + ".for");
		  File fl2_f = new File(filepath_2 + ".for");

		  if(!(display_2.substring(display_2.length()-3)).equals("sum")){		  
		    filein1 = new BufferedReader(new FileReader(filepath_1));
		    filein2 = new BufferedReader(new FileReader(filepath_2));		      
		  }
		  else{
		    filein1 = new BufferedReader(new FileReader(filepath_2));
		    filein2 = new BufferedReader(new FileReader(filepath_1));
		  }
		  
 		  String s1 = filein1.readLine();
		  String s2 = filein2.readLine();
		  boolean combinable = true;
		  StringTokenizer st1 = new StringTokenizer(s1);
		  StringTokenizer st2;
		  while(st1.hasMoreTokens()){
		    String temp_test = st1.nextToken();
		    if(s2.indexOf(temp_test)>0){
		      combinable = false;
		      break;
		    }
		  }
		  
		  while(combinable && (s1 != null && s2 != null)){
		    st1 = new StringTokenizer(s1);
		    st2 = new StringTokenizer(s2);
		    String ft1 = st1.nextToken();
		    String ft2 = st2.nextToken();
		    if(ft1.equals(ft2)){
		      content = content.concat(s1 + "\t");
		      content = content.concat(s2.substring(ft2.length())+"\n");
		      combinable = true;
		    }
		    else{
		      combinable = false;
		      break;
		    }
		    s1 = filein1.readLine();
		    s2 = filein2.readLine();
		  }
		  
		  filein1.close();
		  filein2.close();
		  
		  if(combinable){
		    BufferedWriter fileout = new BufferedWriter(new FileWriter(filepath));
		    fileout.write(content.trim());
		    fileout.newLine();
		    fileout.flush();
		    fileout.close();
		    String fors = new String();
		    if(fl1_f.exists()){
		      BufferedReader fin1_f = new BufferedReader(new FileReader(filepath_1 + ".for"));
		      String s = fin1_f.readLine();
		      while(s != null){
			fors = fors.concat(s + "\n");
			s = fin1_f.readLine();
		      }
		    }
		    if(fl2_f.exists()){
		      BufferedReader fin2_f = new BufferedReader(new FileReader(filepath_2 + ".for"));
		      String s = fin2_f.readLine();
		      while(s != null){
			fors = fors.concat(s + "\n");
			s = fin2_f.readLine();
		      }
		    }
		    if(fors != null){
		      BufferedWriter fileout_f = new BufferedWriter(new FileWriter(filepath+".for"));
		      fileout_f.write(fors.trim());
		      fileout_f.flush();
		      fileout_f.close();
		    }
		  }
		  else{
		    AlertDlg savenotice = new AlertDlg(parent, true, 7);
		  }
		  dispose();
		}
		else{
		}
	      }
	    }
	    catch(Exception er){};
	   
	  }
	});
      
      cancel_b.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    dispose();
	  }
	});
      /*
      Container file_combine = getContentPane();
      file_combine.setLayout(new BoxLayout(file_combine, BoxLayout.Y_AXIS));
      file_combine.add(panel_p3);
      file_combine.add(panel_p4);
      file_combine.add(panel_p2);
      pack();      
      file_combine.setVisible(true);
      */
      add(pan_1, BorderLayout.PAGE_START);
      add(pan_2, BorderLayout.CENTER);
      add(pan_3, BorderLayout.PAGE_END);
    }
  }
}

/********************************
      Table View Data
 ********************************/
class MyTable extends JDialog{
  File sf;
  DialogFrame dlg;
  public MyTable(File f){
    sf = f;
    dlg = new DialogFrame();
    setModal(true);
    getContentPane().add(BorderLayout.CENTER,dlg);
    pack();	
  }  
  class DialogFrame extends JPanel{
    String sfpath = sf.getAbsolutePath();
    JButton ok_b = new JButton("OK");
    JTable table;
    File f_f = new File(sfpath + ".for");

    public DialogFrame(){
      try {
	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception e) { }
      
      Vector colname = new Vector();
      Vector rowData = new Vector();
      Vector rows;
      Frame parent = new Frame("Parent");

      try{
	BufferedReader br = new BufferedReader(new FileReader(sfpath));
	String s = br.readLine();	
	if(s!= null){
	  StringTokenizer st = new StringTokenizer(s);
	  while(st.hasMoreTokens()){
	    colname.addElement(st.nextToken());
	  }
	  s = br.readLine();
	  while(s!=null){
	    rows = new Vector();
	    StringTokenizer stk = new StringTokenizer(s);
	    while(stk.hasMoreTokens()){
	      rows.addElement(stk.nextToken());
	    }
	    rowData.addElement(rows);
	    s = br.readLine();
	  }
	  JTable table = new JTable(rowData, colname);
	  table.setPreferredScrollableViewportSize(new Dimension(100, 70));

	  ok_b.addActionListener(new ActionListener(){
	      public void actionPerformed(ActionEvent e){
		dispose();
	      }
	    });
	  ok_b.setHorizontalTextPosition(JButton.LEFT);
	  
	  JPanel p1 = new JPanel();
	  p1.add(ok_b);

	  JScrollPane scrollPane = new JScrollPane(table,
						   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						   JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	  scrollPane.setPreferredSize(new Dimension(400, 500));
	  
	  Container data_view = getContentPane();
	  data_view.setLayout(new BoxLayout(data_view, BoxLayout.Y_AXIS));
	  data_view.add(scrollPane);
	  data_view.add(p1);
	  pack();      
	  data_view.setVisible(true); 
	}
	else{
	  AlertDlg savenotice = new AlertDlg(parent, true, 8);
	}
      }
      catch(Exception e){
	AlertDlg savenotice = new AlertDlg(parent, true, 8);
      }          
    }
  }
}

/********************************
          Options Setting
********************************/
class OptionDlg extends JDialog{
  DialogFrame dlg;
  ImageIcon[] images;
  boolean actived = false;
  String[] colorStrings = {"white", "black", "red", "blue", "green", "yellow", "cyan", "orange", "pink", "gray"};
  Color[] colorConvert = {Color.white, Color.black, Color.red, Color.blue, Color.green,
			  Color.yellow, Color.cyan, Color.orange, Color.pink, Color.gray};
  String[] colorType = {"Background Color", "Label Color", "Bar Color", "Pattern Color", "Indicator I",
                        "Indicator II", "Indicator III", "Indicator IV", "Indicator V"};
  SysSetting sys = new SysSetting();
  
  public OptionDlg(){
    dlg = new DialogFrame();
    setModal(true);
    getContentPane().add(BorderLayout.CENTER,dlg);
    pack();	
  }
  class ComboBoxRenderer extends JLabel
    implements ListCellRenderer {
    private Font uhOhFont;
    
    public ComboBoxRenderer() {
      setOpaque(true);
      setHorizontalAlignment(CENTER);
      setVerticalAlignment(CENTER);
    }
	
    /*
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     */
    public Component getListCellRendererComponent(
						  JList list,
						  Object value,
						  int index,
						  boolean isSelected,
						  boolean cellHasFocus) {
      //Get the selected index. (The index param isn't
      //always valid, so just use the value.)
      int selectedIndex = ((Integer)value).intValue();
      
      if (isSelected) {
	setBackground(list.getSelectionBackground());
	setForeground(list.getSelectionForeground());
      } else {
	setBackground(list.getBackground());
	setForeground(list.getForeground());
      }
      
      //Set the icon and text.  If icon was null, say so.
      ImageIcon icon = images[selectedIndex];
      setIcon(icon);
      return this;
    }
    
    //Set the font and text when no image was found.
    protected void setUhOhText(String uhOhText, Font normalFont) {
      if (uhOhFont == null) { //lazily create this font
	uhOhFont = normalFont.deriveFont(Font.ITALIC);
      }
      setFont(uhOhFont);
      setText(uhOhText);
    }
  }
  
  class DialogFrame extends JPanel{
    JButton ok_b = new JButton("OK");
    JButton cancel_b = new JButton("Cancel");
    JComboBox[] colorList = new JComboBox[9];

    public DialogFrame(){
      images = new ImageIcon[colorStrings.length];
      Integer[] intArray = new Integer[colorStrings.length];
      for (int i = 0; i < colorStrings.length; i++) {
	intArray[i] = new Integer(i);
	images[i] = new ImageIcon("images/" + colorStrings[i] + ".gif");
	if (images[i] != null) {
	  images[i].setDescription(colorStrings[i]);
	}
      }
      
      //Create the combo box.
      ComboBoxRenderer renderer= new ComboBoxRenderer();
      renderer.setPreferredSize(new Dimension(60, 20));

      for(int i = 0; i < 9; i++){
	colorList[i] = new JComboBox(intArray);
	colorList[i].setRenderer(renderer);
	colorList[i].setMaximumRowCount(3);
      }
      
      for(int i = 0; i < colorConvert.length; i++){
	if(sys.BackgroundColor == colorConvert[i])
	  colorList[0].setSelectedIndex(i);
	if(sys.LabelColor == colorConvert[i])
	  colorList[1].setSelectedIndex(i);
	if(sys.BarColor == colorConvert[i])
	  colorList[2].setSelectedIndex(i);
	if(sys.PatternColor == colorConvert[i])
	  colorList[3].setSelectedIndex(i);
	for(int j = 0; j < sys.MaxInd; j++){
	  if(sys.IndicatorColor[j] == colorConvert[i])
	    colorList[4+j].setSelectedIndex(i);
	}
      }

      ok_b.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    try{
	      BufferedReader stdin_set = new BufferedReader(new FileReader(".\\SystemValue"));
	      String set_content = "";
	      String[] consName = {"BackgroundColor", "LabelColor", "BarColor", 
				   "PatternColor", "IndicatorColor"};
	      boolean checkflag = false;
	      
	      String temp = stdin_set.readLine();
	      while(temp != null){
		for(int i = 0; i < consName.length-1; i++){
		  if(temp.indexOf(consName[i]) >= 0){
		    set_content = set_content + consName[i] + ":" +
		      colorStrings[colorList[i].getSelectedIndex()] + "\n";
		    checkflag = true;
		    break;
		  }
		}
		if(temp.indexOf(consName[consName.length-1])>=0){
		  String indColors = "";
		  for(int i = 4; i < colorList.length-1; i ++){
		    indColors = indColors + colorStrings[colorList[i].getSelectedIndex()] + ",";
		  }
		  indColors = indColors + colorStrings[colorList[colorList.length-1].getSelectedIndex()];  
		  set_content = set_content + consName[consName.length-1] + ":" + indColors + "\n";
		  checkflag = true;
		}
		if(!checkflag)
		  set_content = set_content + temp + "\n";
		checkflag = false;
		temp = stdin_set.readLine();  
	      }
	      stdin_set.close();
	      
	      BufferedWriter stdout_set = new BufferedWriter(new FileWriter(".\\SystemValue"));
	      stdout_set.write(set_content.trim());
	      stdout_set.flush();
	      stdout_set.close();
	    }catch (Exception e1) { }
	    actived = true;
	    dispose();
	  }
	});
      ok_b.setHorizontalTextPosition(JButton.LEFT);

      cancel_b.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    actived = false;
	    dispose();
	  }
	});
      cancel_b.setHorizontalTextPosition(JButton.LEFT);
      
      JPanel[] pan = new JPanel[4];
      for(int i = 0; i < 4; i++){	
	pan[i] = new JPanel(new BorderLayout());
	pan[i].add(colorList[i], BorderLayout.CENTER);
	pan[i].setBorder(
		BorderFactory.createCompoundBorder(
				    BorderFactory.createTitledBorder(colorType[i]),
                                    BorderFactory.createEmptyBorder(20,20,20,20)));
      }
      
      JPanel[] pan_i = new JPanel[5];
      for(int i = 0; i < colorList.length-4; i++){	
	pan_i[i] = new JPanel(new BorderLayout());
	pan_i[i].add(colorList[i+4], BorderLayout.CENTER);
	pan_i[i].setBorder(
		BorderFactory.createCompoundBorder(
				    BorderFactory.createTitledBorder(colorType[i+4]),
                                    BorderFactory.createEmptyBorder(20,20,20,20)));
      }
      
      JPanel pan_colorSetting = new JPanel(new GridLayout(2,2));
      for(int i = 0; i < 4; i++)
	pan_colorSetting.add(pan[i]);
      pan_colorSetting.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Color Setting"),
                                BorderFactory.createEmptyBorder(10,10,10,10)));
      
      JPanel pan_indColorSetting = new JPanel(new GridLayout(2,3));
      for(int i = 0; i < colorList.length-4; i++)
	pan_indColorSetting.add(pan_i[i]);
      pan_indColorSetting.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Indicator Color Setting"),
                                BorderFactory.createEmptyBorder(10,10,10,10)));
      
      JPanel pan_button = new JPanel(new BorderLayout());
      pan_button.add(ok_b, BorderLayout.LINE_START);
      pan_button.add(cancel_b, BorderLayout.LINE_END);

      //add(colorList, BorderLayout.PAGE_START);
      //setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
      
      try {
	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	add(pan_colorSetting, BorderLayout.PAGE_START);
	add(pan_indColorSetting, BorderLayout.CENTER);
	add(pan_button, BorderLayout.PAGE_END);
	/*
	Container data_view = getContentPane();
	data_view.setLayout(new BoxLayout(data_view, BoxLayout.Y_AXIS));
	data_view.add(colorList);
	data_view.add(ok_b);
	pack();      
	data_view.setVisible(true);
	*/
      } catch (Exception e) { }
    }
  }
}

