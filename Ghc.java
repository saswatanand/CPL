import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

public class Ghc{
    BufferedWriter outStream=null;
    final Timer reader = new Timer(1,null);
    boolean readyToTakeCommand=false;   
    String directive = "";
    //Graph graph=null;
    CPL mainWindow = null;
    public Ghc(){
	System.out.println("Don't call without a text area");
    }
   public Ghc(final CommandLine cmdLine, final CPL cpl){

       //this.graph = graph;
       mainWindow = cpl;
       Process child=null;
       
       try{
	   child = Runtime.getRuntime().exec("ghci -fglasgow-exts -package lang -fallow-undecidable-instances -fallow-overlapping-instances -iIndicators -iBuildin_Indicators -iPatterns -iBuildin_Patterns CPL ");
       }catch(IOException e){
	   System.err.println("Failed command: ghci -fglasgow-exts -package lang -fallow-overlapping-instances CPL\n Probably ghci (the haskell interpreter) is not installed on your machine\n or if it is installed it is not in the current path.");
       }

       final BufferedReader inStream= new BufferedReader(
			new InputStreamReader(child.getInputStream()));
       final BufferedReader errorStream = new BufferedReader(
			new InputStreamReader(child.getErrorStream()));
       outStream= new BufferedWriter(
			new OutputStreamWriter(child.getOutputStream()));
  
       
       reader.addActionListener(new ActionListener(){
	       public void actionPerformed(ActionEvent e){
		   char[] buf = new char[1000];
		   int count;		   
		   try{
		       //First Read all from Error Stream
		       //After read all from output stream
		 		
		       while (errorStream.ready()){			   
			   count = errorStream.read(buf,0,1000);
			   System.out.print("Read from Error Stream: ");
			   String output = String.copyValueOf(buf,0,count);
			   System.out.println(output);
			   cmdLine.append(output);
		       }
			
		       if (inStream.ready()){
			   count = inStream.read(buf,0,1000);
			   try{
			       String output = String.copyValueOf(buf,0,count);
			       System.out.print("Read from o/p Stream: ");
			       System.out.println(output);

			       //if(readyToTakeCommand) 
			       //   output = output.substring(output.indexOf('\n')+1);
			       if (output.endsWith("CPL> ")) {
				 readyToTakeCommand = true;
				 reader.stop();
			       }
			       else if (output.indexOf("Failed") >= 0) {
				 mainWindow.actOnDirective("Failed");
			       }
			       else{
				   readyToTakeCommand = false;
			       }
			       
			       // If current output is a part of directive statrted earlier
			       // Or a new directive
			       if (directive.equals("") && !(checkForDirectives(output))){
				   cmdLine.append(output);
			       }else{
				   directive= directive+output;
			       	   if(readyToTakeCommand){
				       int i = directive.lastIndexOf('\n');
				       String tmp = directive.substring(0,i-1); //?
				       System.out.println("Directive is, "+tmp);
				       mainWindow.actOnDirective(tmp);
				       cmdLine.append(directive.substring(i+1));
				       directive="";					       
				   }
			       }
			   }catch(Exception exp){
			       System.out.println("Error in Ghc");
			   }
		       }
		       
		   }catch(IOException exp){
		       System.err.println("Error in Reading:Ghc.java");
		   }
	      }
	   });
       reader.start();
    }

    public boolean checkForDirectives(String result){
        if (result.startsWith("_DIRECTIVE_")){
	    return true;
	}
	return false;
	
    }
    
    public int writeToGhc(String command){
	try{
	    outStream.write(command);
	    outStream.flush();
	    //System.out.println(command);
	    // Dear when it will be run on windows if there is some problem 
	    // Try outStream.newLine()
	    reader.start();
	    return 1;
	}catch(Exception e){
	    System.out.println("Error in writing to Ghc: Ghc.java");
	    return 0;
	}
    }
      
    public static void main(String[] args){
	new Ghc();
    }
}
