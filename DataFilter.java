import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

public class DataFilter extends FileFilter {
	
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.sum)) {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public String getDescription() {
        return "Data Files, (*.sum)";
    }
}
