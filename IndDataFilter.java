import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

public class IndDataFilter extends FileFilter {
	
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.ind)) {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public String getDescription() {
        return "Indicator Data Files, (*.ind)";
    }
}
