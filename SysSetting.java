import java.awt.*;
import java.util.*;
import java.io.*;

public class SysSetting{
  Color BackgroundColor;
  Color LabelColor;
  Color BarColor;
  Color PatternColor;
  Color[] IndicatorColor = new Color[5];

  int MaxBar;
  int MaxInd;
  
  String[] colorStrings = {"white", "black", "red", "blue", "green", "yellow", "cyan", "orange", "pink", "gray"};
  Color[] colorConvert = {Color.white, Color.black, Color.red, Color.blue, Color.green,
			  Color.yellow, Color.cyan, Color.orange, Color.pink, Color.gray};
  
  String[] intStrings = {"MaxBar", "MaxInd"};
  int[] intValue = new int[2];

  String[] valueName = {"Background", "LabelColor", "BarColor", "PatternColor"};
  Color[] colorValue = new Color[4];

  public SysSetting(){
    try{
      BufferedReader sys_in = new BufferedReader(new FileReader(".\\SystemValue"));
      String temp = sys_in.readLine();
      //boolean cpl_addcheck = false;
      while(temp != null){
	StringTokenizer st = new StringTokenizer(temp, ":");
	String fst = st.nextToken();
	String lst = st.nextToken();
	
	for(int i = 0; i < intStrings.length; i++){
	  if(fst.indexOf(intStrings[i])>=0)
	    intValue[i] = Integer.parseInt(lst);
	}
	for(int i = 0; i < valueName.length; i++){	  
	  if(fst.indexOf(valueName[i])>=0){
	    for(int j = 0; j<colorStrings.length; j++){
	      if(lst.equals(colorStrings[j]))
		colorValue[i] = colorConvert[j];
	    }
	  }
	}
	if(fst.indexOf("IndicatorColor")>=0){
	  StringTokenizer st1 = new StringTokenizer(lst, ",");
	  for(int i  = 0; i < 5; i++){
	    String tempColor = st1.nextToken();
	    for(int j = 0; j<colorStrings.length; j++){
	      if(tempColor.equals(colorStrings[j]))
		IndicatorColor[i] = colorConvert[j];
	    }
	  }
	}
	temp = sys_in.readLine();
      }
      MaxBar = intValue[0];
      MaxInd = intValue[1];
      BackgroundColor = colorValue[0];
      LabelColor = colorValue[1];
      BarColor = colorValue[2];
      PatternColor = colorValue[3];
    }
    catch (Exception e){
      System.out.println("System Value Can't Be read in!");
    }
  }  
}
