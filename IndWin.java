import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.util.StringTokenizer;
import java.awt.event.*;

public class IndWin extends JDialog{
  SysSetting sys = new SysSetting();
  JPanel ind,yAxis;
  JScrollPane scroller;
  float[][] indResult = new float[sys.MaxInd][sys.MaxBar];
  MyDate[] date = new MyDate[sys.MaxBar];
  String[] ti_names = new String[sys.MaxInd];
  
  int noOfPoints = 0, noInd = 0;
  int maxY=0,minY=0,midY=0,originY=0, ground=0, top=0, bottom=0;
  int gap=7,lowerMargin = 20,upperMargin=20,volHeight=50;
  float unitY=0;
  int fstVisiblePt, lstVisiblePt;
  String currentpath = "";
 
  public IndWin(final CPL cpl, float[] ti_datas, MyDate[] ti_date, int noBars, String ti_name, String currentdir){ 
    super(cpl,false);
    this.indResult[0] = ti_datas;
    this.date = ti_date;
    this.noOfPoints = noBars;
    this.ti_names[0] = ti_name;
    this.currentpath = currentdir;
    noInd = 1;
    ind = new Ind();
    ind.setBackground(sys.BackgroundColor);
    //ind.setPreferredSize(new Dimension(551,365));

    JMenuBar toolBar = new JMenuBar();
    toolBar.setSize(new Dimension(600, 30));
    //setJMenuBar(menuBar);

    //ceate the first menu (file)
    JButton b_save = new JButton("Save");
    b_save.getAccessibleContext().setAccessibleDescription("File Related");
    b_save.setToolTipText("Save New Indicator Result");
    b_save.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  Frame parent = new Frame("Parent");
	  //////////////////////////////////////////////////	
	  JFileChooser saver = new JFileChooser(currentpath);
	  //////////////////////////////////////////////////
	  saver.addChoosableFileFilter(new IndDataFilter());
	  saver.setAcceptAllFileFilterUsed(false);
	  int returnVal = saver.showSaveDialog(getContentPane());
	  if(returnVal == JFileChooser.APPROVE_OPTION){
	    File file = saver.getSelectedFile();
	    boolean overwrite  = true;
	    if(file.exists()){
	      AlertDlg ad = new AlertDlg(parent, true, 3);
	      if(ad.action_a)
		overwrite = true;
	      else
		overwrite = false;
	    }
	    if(overwrite){
	      String sFileName = file.getName();
	      String sCurrDir = file.getAbsolutePath();
	      
	      StringTokenizer st = new StringTokenizer(sCurrDir, ".");
	      String tmp = "";
	      while(st.hasMoreTokens())
		tmp = st.nextToken();
	      if(!tmp.equals("ind")){
		sFileName = sFileName + ".ind";
		sCurrDir = sCurrDir + ".ind";
	      }
	      try{
		BufferedWriter fileout = new BufferedWriter(new FileWriter(sCurrDir));
		//BufferedWriter fileout_for = new BufferedWriter(new FileWriter(fileDir+".for"));
		String temp = new String();
		//String temp_f = new String();
		
		temp = temp.concat("date\t");
		for(int i = 0; i< noInd; i++){
		  temp = temp.concat(ti_names[i] + "\t");
		  //temp_f = temp_f.concat(ind_names[i] + " : " + funcs[i] + "\n");
		}
		
		fileout.write(temp.trim());
		fileout.newLine();
		for(int i = 0; i<noOfPoints; i++){
		  temp = new String();
		  temp = temp.concat(date[i]+ "\t");
		  for(int j = 0; j< noInd; j++){
		    temp = temp.concat(indResult[j][i]+"\t");
		  }
		  fileout.write(temp.trim());
		  fileout.newLine();
		}
		//fileout_for.write(temp_f.trim());
		//fileout_for.flush();
		//fileout_for.close();
		fileout.flush();
		fileout.close();
		AlertDlg savenotice = new AlertDlg(parent, true, 6);
	      }
	      catch (Exception er){
		System.err.println("Write error: "+er.getMessage());
	      }
	    }
	    else{}
	  }
	}
      });
    toolBar.add(b_save);

    
    //a group of JMenuItems
    JButton b_in = new JButton("<-->");
    b_in.setToolTipText("Expand Time Axis");
    b_in.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if (gap < 10){
	    gap = gap+1;
	    ind.repaint();
	  }
	}
      });
    toolBar.add(b_in);

    JButton b_out = new JButton("-><-");
    b_out.setToolTipText("Compress Time Axis");
    b_out.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if (gap > 1){
	    gap = gap-1;
	    ind.repaint();
	  }
	}
      });
    toolBar.add(b_out);

    JButton b_name = new JButton("Rename");
    b_name.setToolTipText("Rename Indicators");
    b_name.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  MyIndName dlg = new MyIndName();	
	  dlg.setSize(400, 300);
	  dlg.setTitle("Rename Indicator");
	  dlg.show();
	  if(dlg.actived)
	    ind.repaint();
	}
      });
    toolBar.add(b_name);

    scroller = new JScrollPane(ind);
    //scroller.setPreferredSize(new Dimension(getWidth(), getHeight()));

    scroller.setBorder(null);
    JScrollBar hScrollbar = scroller.getHorizontalScrollBar();
    hScrollbar.addAdjustmentListener(new AdjustmentListener(){
	public void adjustmentValueChanged(AdjustmentEvent e){
	  ind.repaint(ind.getVisibleRect());
	}
      });

    yAxis = new YAxis();
    yAxis.setBackground(sys.BackgroundColor);
    yAxis.setPreferredSize(new Dimension(40,400));
    scroller.setPreferredSize(new Dimension(560,400));
    
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar,BorderLayout.NORTH);
    getContentPane().add(yAxis,BorderLayout.WEST);
    getContentPane().add(scroller,BorderLayout.CENTER);
    
    pack();
    setVisible(true);
  }
  
  public IndWin(final CPL cpl, float[][] ti_datas, MyDate[] ti_date, String[] ti_name, 
		int noBars, int[] choice, int indicatorNo, String currentdir){ 
    super(cpl,false);
    int j = 0;
    for(int i = 0; i < indicatorNo; i++){
      if(choice[i] == 1){
	this.indResult[j] = ti_datas[i];
	this.ti_names[j] = ti_name[i];
	j++;
      }
    }	
    this.date = ti_date;
    this.noOfPoints = noBars;
    this.noInd = j;
    this.currentpath = currentdir;

    ind = new Ind();
    ind.setBackground(sys.BackgroundColor);

    JMenuBar toolBar = new JMenuBar();
    toolBar.setSize(new Dimension(600, 30));
    //setJMenuBar(menuBar);

    //ceate the first menu (file)
    JButton b_save = new JButton("Save");
    b_save.getAccessibleContext().setAccessibleDescription("File Related");
    b_save.setToolTipText("Save New Indicator Result");
    b_save.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  Frame parent = new Frame("Parent");
	  //////////////////////////////////////////////////	
	  JFileChooser saver = new JFileChooser(currentpath);
	  //////////////////////////////////////////////////
	  saver.addChoosableFileFilter(new IndDataFilter());
	  saver.setAcceptAllFileFilterUsed(false);
	  int returnVal = saver.showSaveDialog(getContentPane());
	  if(returnVal == JFileChooser.APPROVE_OPTION){
	    File file = saver.getSelectedFile();
	    boolean overwrite  = true;
	    if(file.exists()){
	      AlertDlg ad = new AlertDlg(parent, true, 3);
	      if(ad.action_a)
		overwrite = true;
	      else
		overwrite = false;
	    }
	    if(overwrite){
	      String sFileName = file.getName();
	      String sCurrDir = file.getAbsolutePath();
	      
	      StringTokenizer st = new StringTokenizer(sCurrDir, ".");
	      String tmp = "";
	      while(st.hasMoreTokens())
		tmp = st.nextToken();
	      if(!tmp.equals("ind")){
		sFileName = sFileName + ".ind";
		sCurrDir = sCurrDir + ".ind";
	      }
	      try{
		BufferedWriter fileout = new BufferedWriter(new FileWriter(sCurrDir));
		//BufferedWriter fileout_for = new BufferedWriter(new FileWriter(fileDir+".for"));
		String temp = new String();
		//String temp_f = new String();
		
		temp = temp.concat("date\t");
		for(int i = 0; i< noInd; i++){
		  temp = temp.concat(ti_names[i] + "\t");
		  //temp_f = temp_f.concat(ind_names[i] + " : " + funcs[i] + "\n");
		}
		
		fileout.write(temp.trim());
		fileout.newLine();
		for(int i = 0; i<noOfPoints; i++){
		  temp = new String();
		  temp = temp.concat(date[i]+ "\t");
		  for(int j = 0; j< noInd; j++){
		    temp = temp.concat(indResult[j][i]+"\t");
		  }
		  fileout.write(temp.trim());
		  fileout.newLine();
		}
		//fileout_for.write(temp_f.trim());
		//fileout_for.flush();
		//fileout_for.close();
		fileout.flush();
		fileout.close();
		
		AlertDlg savenotice = new AlertDlg(parent, true, 6);
	      }
	      catch (Exception er){
		System.err.println("Write error: "+er.getMessage());
	      }
	    }
	    else{}
	  }
	}
      });
    toolBar.add(b_save);
    
    //a group of JMenuItems
    JButton b_in = new JButton("<-->");
    b_in.setToolTipText("Expand Time Axis");
    b_in.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if (gap < 10){
	    gap = gap+1;
	    ind.repaint();
	  }
	}
      });
    toolBar.add(b_in);

    JButton b_out = new JButton("-><-");
    b_out.setToolTipText("Compress Time Axis");
    b_out.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  if (gap > 1){
	    gap = gap-1;
	    ind.repaint();
	  }
	}
      });
    toolBar.add(b_out);
    
    JButton b_name = new JButton("Rename");
    b_name.setToolTipText("Rename Indicators");
    b_name.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
	  MyIndName dlg = new MyIndName();	
	  dlg.setSize(400, 300);
	  dlg.setTitle("Rename Indicators");
	  dlg.show();
	  if(dlg.actived)
	    ind.repaint();
	}
      });
    toolBar.add(b_name);

    scroller = new JScrollPane(ind);
    scroller.setBorder(null);
    JScrollBar hScrollbar = scroller.getHorizontalScrollBar();
    hScrollbar.addAdjustmentListener(new AdjustmentListener(){
	public void adjustmentValueChanged(AdjustmentEvent e){
	  ind.repaint(ind.getVisibleRect());
	}
      });

    yAxis = new YAxis();
    yAxis.setBackground(sys.BackgroundColor);
    yAxis.setPreferredSize(new Dimension(40,400));

    scroller.setPreferredSize(new Dimension(560,400));

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(yAxis,BorderLayout.WEST);
    getContentPane().add(scroller,BorderLayout.CENTER);
    getContentPane().add(toolBar, BorderLayout.NORTH);

    pack();
    setVisible(true);
  }

  class Ind extends JPanel{
    protected void paintComponent(Graphics g){
      if (noOfPoints==0) return;
      int ground=0;
      setPreferredSize(new Dimension(noOfPoints*gap+10,
				     scroller.getHeight()-15));
      revalidate();
      super.paintComponent(g);
      ground = getHeight()/2;
      bottom = getHeight() - lowerMargin;
      top = upperMargin;
      g.setColor(sys.LabelColor);
      g.drawLine(0, bottom, gap * noOfPoints, bottom);
      Rectangle rect = getVisibleRect();
      fstVisiblePt = Math.max((int)(rect.x/gap),1);
      lstVisiblePt = (int)((rect.x+rect.width)/gap);
      if (lstVisiblePt > noOfPoints) lstVisiblePt=noOfPoints;
      int tempMin=10000,tempMax=0;
      for(int j=0; j<noInd; j++){
	for(int i=fstVisiblePt;i<=lstVisiblePt;i++){
	  if (indResult[j][i-1] != -1){
	    if (indResult[j][i-1]*100 > tempMax){
	      tempMax = (int)Math.ceil((double)indResult[j][i-1]*100)+10;
	    }
	    if (indResult[j][i-1]*100 < tempMin){
	      tempMin = (int)(indResult[j][i-1]*100)-10;
	    }	
	  }
	}		 
      }
      boolean flag = false;
      if (tempMax != maxY || tempMin != minY){
	flag = true;
	maxY = tempMax;
	minY = tempMin;
      }
      midY = (maxY+minY)/2;
      unitY = (float)(bottom-top) / (maxY-minY);

      if(Math.abs(midY/100)<1){
	g.setColor(sys.LabelColor);
	g.drawLine(gap,ground-(int)((-midY)*unitY),
		   noOfPoints*gap,ground-(int)((-midY)*unitY));
      }

      int widthOfWings = (int)((gap-2)/2);
      int startx = rect.x;
      int label_length = 50;
      for(int j=0;j<noInd;j++){
	g.setColor(sys.IndicatorColor[j]);
	g.drawLine(startx,10,startx+label_length,10);
	g.drawString(ti_names[j], startx+label_length+5, 15);
	for(int i=1;i<noOfPoints;i++){
	  if(indResult[j][i-1] != -1)
	    g.drawLine(i*gap,ground-(int)((indResult[j][i-1]*100-midY)*unitY),
		       (i+1)*gap,ground-(int)((indResult[j][i]*100-midY)*unitY));
	}
	startx = startx + label_length + ti_names[j].length()*10;
      }
      
      // Draw the X axis Legend
      g.setColor(sys.LabelColor);
      int current = 0;
      int lastBigTick = 0;
      for(int i=1;i<=noOfPoints;i++){
	if (date[i-1].month != current){
	  g.drawLine(i*gap,bottom,i*gap,bottom+7);
		      current = date[i-1].month;
		      if(i < fstVisiblePt+10 && i  > fstVisiblePt-13){
			g.drawString(MyDate.getMonthName(current)+"'"+Integer.toString(date[i-1].year),
				     (i+10)*gap,bottom+15);
		      }else{
			g.drawString(MyDate.getMonthName(current),(i+10)*gap,bottom+15);
		      }
		      lastBigTick=i;
		      
	}else{
	  g.drawLine(i*gap,bottom,i*gap,bottom+3);
	}
      }
      if (flag) yAxis.repaint();
    }
  }
  
  class YAxis extends JPanel{
    int preferredNoOfTicks=10;
	protected void paintComponent(Graphics g){
	    if (noOfPoints==0) return;
	    int count=1;
	    int width=getWidth();
	    super.paintComponent(g);
	    g.setColor(sys.LabelColor);
	    g.drawLine(width-1,upperMargin,
                       width-1,getHeight()-lowerMargin-15);
	    float temp = (float)(maxY-minY);
	    if (temp > preferredNoOfTicks){
		while(temp > preferredNoOfTicks) temp = (float)(maxY-minY)/++count;
		if (2*preferredNoOfTicks >= (maxY-minY)/(count-1)+(int)temp) count=count-1;
		g.drawLine(width-1,bottom,width-3,bottom);
		for(int i=minY+(maxY-minY)%count;i<=maxY;i=i+count){
		    g.drawLine(width-1, bottom-(int)((i-minY)*unitY),
			       width-3,bottom-(int)((i-minY)*unitY));
		    g.drawString(Float.toString((float)i/100),
				 width-40,bottom-(int)((i-minY)*unitY)+5);
		}
	   }else if(temp > 0){
	       while(temp < preferredNoOfTicks){
		   count = count*2;
		   temp = (float)(maxY-minY)*count;
	       }
	       if(2*preferredNoOfTicks <= (maxY-minY)*(count/2)+(int)temp) count=count/2;
	       for(float i=minY;i<=maxY;i=i+1/(float)count){
		   g.drawLine(width-1,bottom-(int)((i-minY)*unitY),
			      width-3,bottom-(int)((i-minY)*unitY));
		   g.drawString(Float.toString((float)i/100),width-40,bottom-(int)((i-minY)*unitY)+5);
	       }	      
	   }
	}
  }
  
  class MyIndName extends JDialog{
    boolean actived = false;
    DialogFrame dlg;
    
    public MyIndName(){
      dlg = new DialogFrame();
      dlg.setName("Rename Indicator Result");
      setModal(true);
      getContentPane().add(BorderLayout.CENTER,dlg);
      pack();	
    }
    
    class DialogFrame extends JPanel{
      JLabel[] ti_l = new JLabel[sys.MaxInd];
      JTextField[] ti_t = new JTextField[sys.MaxInd];
      Frame parent = new Frame("Parent");
      
      private void addLabelTextRows(JLabel[] labels,
				    JTextField[] textFields,
				    GridBagLayout gridbag,
				    Container container) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        int numLabels = labels.length;
	
        for (int i = 0; i < numLabels; i++) {
	  c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
	  c.fill = GridBagConstraints.NONE;      //reset to default
	  c.weightx = 0.0;                       //reset to default
	  container.add(labels[i], c);
	  
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
	
	for(int i = 0; i<sys.MaxInd; i++){
	  ti_l[i] = new JLabel("Indicator " +(i+1)+ ":  ");
	}
	for(int i = 0; i<sys.MaxInd; i++){
	  ti_t[i] = new JTextField(ti_names[i], 20);
	  ti_l[i].setLabelFor(ti_t[i]);
	}
	JButton b_rename = new JButton("Rename");
	b_rename.setToolTipText("Rename Indicators");
	b_rename.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent e){
	      for(int i = 0; i < sys.MaxInd; i++){
		if(ti_t[i].getText() != null){
		  ti_names[i] = ti_t[i].getText();
		}
	      }
	      actived = true;
	      dispose();	    
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

        JLabel[] labels = new JLabel[noInd];
	for(int i = 0; i < noInd; i++)
	  labels[i] = ti_l[i];

        JTextField[] textFields = new JTextField[noInd];
	for(int i = 0; i < noInd; i++)
	  textFields[i] = ti_t[i];

        addLabelTextRows(labels, textFields, gridbag, textControlsPane);

        c.gridwidth = GridBagConstraints.REMAINDER; //last
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        textControlsPane.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Rename Indicator"),
                                BorderFactory.createEmptyBorder(5,5,5,5)));

	JPanel leftPane = new JPanel(new GridLayout(1,0));
        leftPane.add(textControlsPane);

	JPanel rightPane = new JPanel(new BorderLayout());
        rightPane.add(b_rename,BorderLayout.LINE_START);
        rightPane.add(cancel,BorderLayout.LINE_END);


	add(leftPane, BorderLayout.PAGE_START);
        add(rightPane, BorderLayout.PAGE_END);
      }
    }
  }
}
