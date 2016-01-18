
import java.io.*;
import java.util.StringTokenizer;


class MyDate{
    int date;
    int month;
    int year;
    String dateToString;

    public MyDate(String str){
    	dateToString = str;
	StringTokenizer st = new StringTokenizer(str,"//");
	month = Integer.parseInt(st.nextToken());
	date = Integer.parseInt(st.nextToken());
	year = Integer.parseInt(st.nextToken());
    }

    static String getMonthName(int mnt){
	String[] ms = {"Jan","Feb","Mar","Apr","May","Jun",
		       "Jul","Aug","Sep","Oct","Nov","Dec"};
	return ms[mnt-1];
    }
    
    public String toString(){
    	return dateToString;
    }
}
	    
