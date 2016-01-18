import java.io.*;
import java.util.StringTokenizer;

public class MyReader{
    BufferedReader br = null;
   
    public MyReader(String company){
	try{
	    br = new BufferedReader(new FileReader(company));
       	   
	}catch(FileNotFoundException e){
	    System.out.println("File not found");
	}
    }
    public int read(MyDate[] date, float[] open,float[] high, float[] low,
		float[] close, double[] volume){
	int count=0;
	String line;
	//Date = new String[250];
	try{
	    line = br.readLine();
	    while ((line = br.readLine()) != null){
		StringTokenizer st = new StringTokenizer(line," ");
		date[count]=new MyDate(st.nextToken());
		open[count]=Float.parseFloat(st.nextToken());
		high[count]=Float.parseFloat(st.nextToken());
		low[count] =Float.parseFloat(st.nextToken());
		close[count]=Float.parseFloat(st.nextToken());
		volume[count]=Double.parseDouble(st.nextToken());
		
		count++;
	    }
	}catch (IOException e){
		System.out.println("Something else");
        }
	return count;
    }
	
}












