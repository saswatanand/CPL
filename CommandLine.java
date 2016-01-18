import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class CommandLine extends JScrollPane {
                         
    JTextArea textArea = new JTextArea();
    String command = new String();
    Ghc ghc = null;
    int lastStop=0;
    String[] commands = new String[100];
    int commandNo = 0;
    int commandShowing=0;
    Action defaultBackSpaceAction;
       
    public CommandLine(final CPL cpl){
	Graph graph = cpl.graph;
	setViewportView(textArea);
	textArea.setLineWrap(true);
	textArea.setWrapStyleWord(true);
	textArea.addKeyListener(new MyKeyAdapter());
	textArea.addCaretListener(new CaretListener(){
		public void caretUpdate(CaretEvent e){
		    //System.out.println(e.getDot());
		    if(e.getDot() == 0) return;
		    if (e.getDot() < lastStop){
			//System.out.println(lastStop);
			//System.out.println(textArea.getText().length());
			textArea.setCaretPosition(lastStop);
		    }
		}
		});
	
	Keymap map = JTextComponent.addKeymap("My Map",textArea.getKeymap());

	KeyStroke left = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0,false);
	KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP,0,false);
	KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0,false);
	KeyStroke backspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,0,false);
	KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0,false);

        defaultBackSpaceAction = map.getAction(backspace);
	if(map.getAction(backspace) == null) System.out.println("Surprising");
        map.addActionForKeyStroke(left,new MyLeftAction());
	map.addActionForKeyStroke(up,new MyUpAction());
	map.addActionForKeyStroke(down,new MyDownAction());
	map.addActionForKeyStroke(backspace,new MyBackSpaceAction());
	map.addActionForKeyStroke(enter, new MyEnterAction());
	textArea.setKeymap(map);
	
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	setBorder(BorderFactory.createEtchedBorder());
	
	ghc = new Ghc(this,cpl);
    }
    
    public void append(String output)
    {
	int maxLength = 8000;
	String current = textArea.getText();
	String newS = current + output;
	int len = newS.length();
	if (len > maxLength){
	    textArea.setText(newS.substring(len-maxLength));
	}
        else textArea.append(output);
	lastStop = textArea.getText().length();
	//System.out.println(lastStop);
       	textArea.setCaretPosition(lastStop);
    }

    class MyLeftAction extends AbstractAction{
	public MyLeftAction(){ super("Left Action");}
	public void actionPerformed(ActionEvent ev){
	    //System.out.println(caret.getDot());
	    int pos = textArea.getCaretPosition();
	    if (pos > lastStop) textArea.setCaretPosition(pos-1);
	    else Toolkit.getDefaultToolkit().beep();
	}
    }
    class MyUpAction extends AbstractAction{
	public MyUpAction(){ super("Up Action");}
	public void actionPerformed(ActionEvent ev){
	    if(commandShowing > 0){
	        int l = textArea.getText().length();	
		textArea.replaceRange(commands[--commandShowing],l-command.length(),l);
		command = commands[commandShowing];
     	    }
      	}
    }
    class MyDownAction extends AbstractAction{
	public MyDownAction(){ super("Down Action");}
	public void actionPerformed(ActionEvent ev){
	    if(commandShowing < commandNo){
		int l = textArea.getText().length();
		int cl = command.length();
		if(commandShowing == commandNo-1){
		    command ="";
		    commandShowing++;
		}
		else if(commandShowing < (commandNo-1))
		    command = commands[++commandShowing];
	        textArea.replaceRange(command,l-cl,l);
	    }
      	}
    }

    class MyEnterAction extends AbstractAction{
	public MyEnterAction(){super("Enter Action");}
	public void actionPerformed(ActionEvent ev){
	    int pos = textArea.getCaretPosition();
	    if (pos > lastStop) textArea.setCaretPosition(textArea.getText().length());
	    textArea.append("\n");
	}
    }
	
    class MyBackSpaceAction extends AbstractAction{
	public MyBackSpaceAction(){super("BackSpace Action");}
	public void actionPerformed(ActionEvent ev){
	    int pos = textArea.getCaretPosition();
	    //System.out.println(pos);
	    if(pos >= lastStop){
		if (defaultBackSpaceAction == null); 
		  //System.out.println("It is NULL");
		else defaultBackSpaceAction.actionPerformed(ev);
		//System.out.println("OK"); 
		//String s = textArea.getText();
		//System.out.println(s);
		//textArea.setText(s.substring(0,s.length()));
		//System.out.println("********************");
		//System.out.println(textArea.getText().length());
	    }
	    else {Toolkit.getDefaultToolkit().beep();System.out.println("nomore");}
	}
    }
   
    public void setActiveChart(String file){
	ghc.writeToGhc("setActiveChart \""+file+"\"\n");
    }
    
    public void runHaskellCmd(String cmd){
    	append(cmd);
    	ghc.writeToGhc(cmd);
    }

    class MyKeyAdapter extends KeyAdapter{
	/** Handle the key typed event from the text field. */
	public void keyTyped(KeyEvent e) {
	    /*char key = e.getKeyChar();
	    if (key=='\b'){
		command = command.substring(0,command.length()-1);
		//System.out.println("Back space");
	    }
	    else command = command + key;*/
	    
	}
	/** Handle the key released event from the text field. */
	public void keyReleased(KeyEvent e) {
	   if(e.getKeyCode()== KeyEvent.VK_ENTER){
		//System.out.print(command);
	        //get the command
               String text = textArea.getText();
	       int pos = textArea.getCaretPosition();
	       if (pos > lastStop) textArea.setCaretPosition(text.length()); 
	       
	       String command = text.substring(lastStop,text.length());
		ghc.writeToGhc(command);
		if (command.length() > 1){
		    commands[commandNo++] =command.substring(0,command.length()-1);
		    commandShowing = commandNo;
		}
		//command = "";
	   }
	   
	    
	}
    }
    
}
