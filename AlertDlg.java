import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

/****************************
        Alert Dialog
*****************************/
class AlertDlg{
  boolean action_a;
  public AlertDlg(Frame parent, boolean model, int flag){
    if(flag == 0){
      JOptionPane.showMessageDialog(parent, "No Indicator Result!",
      				    "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if(flag == 1){
      JOptionPane.showMessageDialog(parent, "Please load company data first!", 
      				    "Warning", JOptionPane.WARNING_MESSAGE);
    }
    else if(flag == 2){
      JOptionPane.showMessageDialog(parent, "You have reach the Maximun!");
    }
    else if(flag == 3){
      int n = JOptionPane.showConfirmDialog(parent, "The File Already Exist!",
				    "Overwrite it?", JOptionPane.YES_NO_OPTION);
      if (n == JOptionPane.YES_OPTION) {
	action_a = true;
      } 
      else if (n == JOptionPane.NO_OPTION) {
	action_a = false;
      } 
      else {
      }
    }
    else if(flag == 4){
      JOptionPane.showMessageDialog(parent, "CPL System  v0.3");
    }
    else if(flag == 5){
      JOptionPane.showMessageDialog(parent, "The Field Can't Be Blank!",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
    }
    else if(flag == 6){
      JOptionPane.showMessageDialog(parent, "File Saved!");
    }
    else if(flag == 7){
      JOptionPane.showMessageDialog(parent,"The 2 Files Can't Be Combined. Same Column Name or Date not match!", 
				    "Error",JOptionPane.ERROR_MESSAGE);
    }
    else if(flag == 8){
      JOptionPane.showMessageDialog(parent, "The File Can't Display!",
				    "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if(flag == 9){
      int n = JOptionPane.showConfirmDialog(parent, "Error with New Indicator/Pattern!",
				    "Remove it form the List?", JOptionPane.YES_NO_OPTION);
      if (n == JOptionPane.YES_OPTION) {
	action_a= true;
      } 
      else if (n == JOptionPane.NO_OPTION) {
	action_a = false;
      } 
      else {
      }
      // JOptionPane.showMessageDialog(parent, "New Indicator/Pattern can't be loaded!",
      //			    "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if(flag == 10){
      JOptionPane.showMessageDialog(parent, "System Need to Be Recompiled!",
				    "Warning", JOptionPane.WARNING_MESSAGE);
    }
    else{}
      //pane.add(button1);
  }
  public boolean getWrite(){
    return action_a;
  }
}
