import java.io.*;
import java.util.StringTokenizer;

public class MyIndReader{
  BufferedReader br = null;
  int count_ind = 0;
  String datafile = "";
  
  public MyIndReader(String IndData){
    try{
      br = new BufferedReader(new FileReader(IndData));       	   
    }catch(FileNotFoundException e){
	    System.out.println("File not found");
    }
  }
  public int read(String[] ind_names, MyDate[] date, float[][] ind_datas){
    int count=0;
    String line;
    //Date = new String[250];
    try{
	  line = br.readLine();
	  datafile = line;
	  line = br.readLine();
	  StringTokenizer st0 = new StringTokenizer(line,"\t");
	  String temp = st0.nextToken();
	  while(st0.hasMoreTokens()){
	    ind_names[count_ind] = st0.nextToken();
	    count_ind++;
	  }		
	  while ((line = br.readLine()) != null){
	    StringTokenizer st = new StringTokenizer(line,"\t");
	    int tmp = 0;
	    date[count]=new MyDate(st.nextToken());
	    while(st.hasMoreTokens()){
	      ind_datas[tmp][count] = Float.parseFloat(st.nextToken());
	      tmp++;
	      
	      count++;
	    }
	  }
	}catch (IOException e){
	  System.out.println("Something else");
        }
	return count;
    }
  public int getIndicatorNo(){
    return count_ind;
  }
  public String getDataFilePath(){
    return datafile;
  }
  
}
