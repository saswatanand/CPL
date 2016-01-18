import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;

class Graph extends JPanel{
  SysSetting sys = new SysSetting();
  MyDate[] date= new MyDate[sys.MaxBar];
  float[] open = new float[sys.MaxBar];
  float[] high = new float[sys.MaxBar];
  float[] low = new float[sys.MaxBar];
  float[] close = new float[sys.MaxBar];
  double[] volume =new double[sys.MaxBar];
  float[][] ti = new float[sys.MaxInd][sys.MaxBar];
  
  String[] ti_formula = new String[sys.MaxInd];
  String[] ind_names = new String[sys.MaxInd]; 
  int[] start_d = new int[sys.MaxInd];
  int[] size_d = new int[sys.MaxInd];
  
  final int label_length = 50;
  
  JPanel chart,yAxis,indArea,indYAxis,dateView;
  JPanel pAxis, pArea;
  JScrollPane scroller, scrollerAxis;
  int noOfPoints=0;
  int maxY=0,minY=0,originY=0, ground=0;
  int gap=7,lowerMargin = 20,upperMargin=20,volHeight=50;
  float unitY=0;
  int fstVisiblePt, lstVisiblePt;
  String granularity = "";
  String currentChart = "";
  boolean TechDraw = false;
  int IndicatorNo = 0;

  public Graph(){
    for(int i = 0; i < sys.MaxInd; i++){
      ti_formula[i] = "";
      ind_names[i] = "TI" + (i+1);
      start_d[i] = 0;
      size_d[i] = 0;
    }
    pAxis = new JPanel();
    pAxis.setPreferredSize(new Dimension(40, getHeight()));

    chart = new Chart();
    chart.setBackground(sys.BackgroundColor);
    
    yAxis = new YAxis();
    yAxis.setBackground(sys.BackgroundColor);
    yAxis.setPreferredSize(new Dimension(40, getHeight()));
    //yAxis.setPreferredSize(new Dimension(40, pAxis.getHeight()*2/3));
    /*
    indArea = new IndArea();
    indArea.setBackground(Color.yellow);
    //indArea.setBackground(sys.BackgroundColor);
    
    indYAxis = new IndYAxis();
    indYAxis.setBackground(Color.yellow);
    indYAxis.setPreferredSize(new Dimension(40, 169));
    //indYAxis.setBackground(sys.getBackgroundColor);
    //indYAxis.setPreferredSize(new Dimension(40, pAxis.getHeight()/3));    
    
    pArea = new JPanel();
    pAxis.setLayout(new BoxLayout(pAxis, BoxLayout.Y_AXIS));
    pArea.setLayout(new BoxLayout(pArea, BoxLayout.Y_AXIS));
    
    pAxis.add(yAxis);
    pAxis.add(indYAxis);
    
    pArea.add(chart);
    pArea.add(indArea);
    */

    scroller = new JScrollPane(chart);
    scroller.setBorder(null);
    JScrollBar hScrollbar = scroller.getHorizontalScrollBar();
    /*AdjustmentListener old = hScrollbar.getAdjustmentListener();
      hScrollbar.removeAdjustmentListener(old);*/
    hScrollbar.addAdjustmentListener(new AdjustmentListener(){
	public void adjustmentValueChanged(AdjustmentEvent e){
	  //chart.repaint(chart.getVisibleRect());
	  //indArea.repaint(indArea.getVisibleRect());
	  chart.repaint(chart.getVisibleRect());
	}
      });

    setLayout(new BorderLayout());
    add(yAxis,BorderLayout.WEST);
    add(scroller,BorderLayout.CENTER);
    //add(test, BorderLayout.EAST);

    //dataView = new DataView();
  }
    
  class Chart extends JPanel{
    protected void paintComponent(Graphics g){
      if (noOfPoints==0) return;
      setPreferredSize(new Dimension(noOfPoints*gap+10,
				     scroller.getHeight()*2/3));
      revalidate();
      super.paintComponent(g);
      ground = getHeight()-lowerMargin;
      g.setColor(sys.LabelColor);
      g.drawLine(0, ground, gap*noOfPoints, ground);
      Rectangle rect = getVisibleRect();
      fstVisiblePt = Math.max((int)(rect.x/gap),1);
      lstVisiblePt = (int)((rect.x+rect.width)/gap);
      if (lstVisiblePt > noOfPoints) lstVisiblePt=noOfPoints;
      int tempMin=10000,tempMax=0;
      for(int i=fstVisiblePt;i<=lstVisiblePt;i++){
	if (high[i-1] > tempMax){
	  tempMax = (int)Math.ceil((double)high[i-1]);
	}
	if (low[i-1] < tempMin){
	  tempMin = (int)low[i-1];
	}			 
      }
      boolean flag = false;
      if (tempMax != maxY || tempMin != minY){
	flag = true;
	maxY = tempMax;
	minY = tempMin;
      }
      originY = getHeight()-lowerMargin-volHeight;
      unitY = (float)(originY - upperMargin) /(maxY-minY);
      g.setColor(sys.BarColor);
      int widthOfWings = (int)((gap-2)/2);
      for(int i=1;i<=noOfPoints;i++){
	g.drawLine(i*gap,originY-(int)((low[i-1]-minY)*unitY),
		   i*gap,originY-(int)((high[i-1]-minY)*unitY));
	g.drawLine(i*gap,originY-(int)((open[i-1]-minY)*unitY),
		   i*gap-widthOfWings,originY-(int)((open[i-1]-minY)*unitY));
	g.drawLine(i*gap,originY-(int)((close[i-1]-minY)*unitY),
		   i*gap+widthOfWings,originY-(int)((close[i-1]-minY)*unitY));
	
      }
      
      // Draw the X axis Legend
      //g.setColor(Color.blue);
      g.setColor(sys.LabelColor);
      int current = 0;
      int lastBigTick = 0;
      for(int i=1;i<=noOfPoints;i++){
	if (date[i-1].month != current){
	  g.drawLine(i*gap,ground,i*gap,ground+7);
	  current = date[i-1].month;
	  if(i < fstVisiblePt+10 && i  > fstVisiblePt-13){
	    g.drawString(MyDate.getMonthName(current)+"'"+Integer.toString(date[i-1].year),
			 (i+10)*gap,ground+15);
	  }else{
	    g.drawString(MyDate.getMonthName(current),(i+10)*gap,ground+15);
	  }
	  lastBigTick=i;
	}else{
	  g.drawLine(i*gap, ground, i*gap, ground+3);
	}
      }
      
      if (flag) yAxis.repaint();
      extrasDraw(g);
      if(TechDraw){
	extraTechDraw(g, rect.x);
      }
      //extraLabelDraw(g, rect.x);
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
		 width-1,ground);
      float temp = (float)(maxY-minY);
      if (temp > preferredNoOfTicks){
	while(temp > preferredNoOfTicks) temp = (float)(maxY-minY)/++count;
	if (2*preferredNoOfTicks >= (maxY-minY)/(count-1)+(int)temp) count=count-1;
	g.drawLine(width-1, originY,width-3,originY);
	for(int i=minY+(maxY-minY)%count;i<=maxY;i=i+count){
	  g.drawLine(width-1, originY-(int)((i-minY)*unitY),
		     width-3,originY-(int)((i-minY)*unitY));
	  g.drawString(Float.toString((float)i),
		       width-40,originY-(int)((i-minY)*unitY)+5);
	}
      }else if(temp > 0){
	while(temp < preferredNoOfTicks){
	  count = count*2;
	  temp = (float)(maxY-minY)*count;
	}
	if(2*preferredNoOfTicks <= (maxY-minY)*(count/2)+(int)temp) count=count/2;
	for(float i=minY;i<=maxY;i=i+1/(float)count){
	  g.drawLine(width-1,originY-(int)((i-minY)*unitY),
		     width-3,originY-(int)((i-minY)*unitY));
	  g.drawString(Float.toString(i),width-40,originY-(int)((i-minY)*unitY)+5);
	}
      }
    }
  }

  int noOfLines = 0;
  float cord [] = new float[200];  //assuming no of lines are < 250
  
  public void extrasDraw(Graphics g){
    int i=0;
    float x1,y1,x2,y2;
    while (i < noOfLines){
      x1 = cord [i*4]; y1 = cord [i*4+1]; x2 = cord[i*4+2]; y2 = cord[i*4+3];
      //if ((x1 < lstVisiblePt) && (x1 > fstVisiblePt || x2 > fstVisiblePt))
      g.drawLine((int)x1*gap,originY-(int)((y1-minY)*unitY),
		 (int)x2*gap,originY-(int)((y2-minY)*unitY));
      /* I assume x2 > x1 */
      i++;
    }
  }
  
  public void addALine(int x, int y){
    cord[noOfLines*4] = x;
    cord[noOfLines*4+1] = close[x-1];
    cord[noOfLines*4+2] = y;
    cord[noOfLines*4+3] = close[y-1];
    noOfLines++;
    chart.repaint();
  }
	
  public void addALineWithLocAndVal(float[] price, int size){
    if(IndicatorNo<0 || IndicatorNo>=sys.MaxInd){}
    else{
      for(int i = 0; i< size; i++){
	ti[IndicatorNo][i] = price[i];
      }
      start_d[IndicatorNo] = 0;
      size_d[IndicatorNo] = size;
    }	
    IndicatorNo++;
    TechDraw = true;
    chart.repaint();	    
  }
  
  public void extraTechDraw(Graphics g, int startx){
    if(IndicatorNo<=0 || IndicatorNo>sys.MaxInd){}
    else{
      for(int i = 0; i < IndicatorNo; i++){
	g.setColor(sys.IndicatorColor[i]);
	g.drawLine(startx,10,startx+label_length,10);
	g.drawString(ind_names[i], startx+label_length+5, 15);
	for(int j = start_d[i]; j<size_d[i]-1; j++){
	  if(ti[i][j]!= -1)
	    g.drawLine((int)(j+1)*gap,originY-(int)((ti[i][j]-minY)*unitY),
		       (int)(j+2)*gap,originY-(int)((ti[i][j+1]-minY)*unitY));
	}
	startx = startx + label_length + ind_names[i].length()*10;
      }
    }
  }
  
  public void loadChart(String file){
    if (!(currentChart.equals(file))){
      MyReader mr = new MyReader(file); 
      noOfPoints = mr.read(date,open,high,low,close,volume);
      currentChart = file;
      noOfLines = 0;
      repaint();
    }
  }
  
  public void loadIndicator(String file){
    if (!(currentChart.equals(file))){
      String filepath = "";
      MyIndReader mr = new MyIndReader(file);
      noOfPoints = mr.read(ind_names, date, ti);
      IndicatorNo = mr.getIndicatorNo();
      TechDraw = true;
      for(int i = 0; i < IndicatorNo; i++){
	start_d[i] = 0;
	size_d[i] = noOfPoints;
      }
      filepath = mr.getDataFilePath();
      loadChart(filepath);
    }
  }
  
  public boolean hasInd(){
    if(IndicatorNo>0)
      return true;
    else
      return false;
  }
    
  public float[][] getTI(){
    return ti;
  }
  
  public float[] getClose(){
    return close;
  }
  
  public float[] getOpen(){
    return open;
  }
  
  public float[] getHigh(){
    return high;
  }
  
  public float[] getLow(){
    return low;
  }
  
  public double[] getVolume(){
    return volume;
  }
  
  public MyDate[] getDate(){
    return date;
  }    
  
  public int[] getStartDay(){
    return start_d;
  }
  
  public int[] getEndDay(){
    return size_d;
  }
  
  public int getIndicatorNo(){
    return IndicatorNo;
  }
  
  public String[] getIndNames(){
    return ind_names;
  }
  
  public String[] getIndFuncs(){
    return ti_formula;
  }

  public void setIndFunc(String func){
    ti_formula[IndicatorNo] = func;
  }
    
  
  public boolean resetInit(){
    for(int i = 0; i < sys.MaxInd; i++){
      ti_formula[i] = "";
      ind_names[i] = "TI" + (i+1); 
      start_d[i] = 0;
      size_d[i] = 0;
    }
    return true;
  }

  class MyMouseListener extends MouseInputAdapter{
    public void mouseReleased(MouseEvent e){
      System.out.println(maxY);
      System.out.println(minY);
      System.out.println(low[fstVisiblePt-1]);
    }
	
    public void mouseMoved(MouseEvent e) {
      saySomething("Mouse moved", e);
    }
	    
    public void mouseDragged(MouseEvent e) {
      saySomething("Mouse dragged", e);
    }
    
    void saySomething(String eventDescription, MouseEvent e) {
      System.out.println(eventDescription 
			 + " (" + e.getX() + "," + e.getY() + ")"
			 + " detected on "
			 + e.getComponent().getClass().getName()
			 + "\n");
      //textArea.setCaretPosition(textArea.getDocument().getLength());
    }
  }
}
