package tudbut.mod.client.ttc.utils.isbpl;

import de.tudbut.io.StreamReader;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author TudbuT
 * @since 01 May 2022
 */

public interface ISBPLScript {
    
    
    ISBPL context();
    
    default Stack<ISBPLObject> run(String code, Object... args) {
        Stack<ISBPLObject> stack = new ISBPLStack();
        for (int i = 0 ; i < args.length ; i++) {
            stack.push(context().toISBPL(args[i]));
        }
        context().interpret(new File("_eval"), code, stack);
        return stack;
    }
    
    default boolean functionExists(String fn) {
        return context().level0.map.containsKey(fn);
    }
    
    class Loader {
        static {
            try {
                URL url = new URL("https://codeload.github.com/TudbuT/isbpl/zip/refs/heads/master");
                ZipInputStream inputStream = new ZipInputStream(url.openStream());
                ZipEntry entry;
                while ((entry = inputStream.getNextEntry()) != null) {
                    File associatedFile = new File("config/ttc/isbpl", entry.getName().substring("isbpl-master/".length()));
                    if(entry.isDirectory()) {
                        associatedFile.mkdirs();
                    }
                    else {
                        associatedFile.getParentFile().mkdirs();
                        FileOutputStream stream = new FileOutputStream(associatedFile);
                        stream.write(new StreamReader(inputStream).readAllAsBytes());
                        stream.close();
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Unable to download ISBPL");
            }
        }
    
        public static void ensureLoaded() {}
        
        public static ISBPL makeContext() throws IOException {
            Stack<ISBPLObject> stack = new ISBPLStack();
            ISBPL context = new ISBPL();
            File std = new File("config/ttc/isbpl/std.isbpl");
            context.interpret(std, ISBPL.readFile(std), stack);
            return context;
        }
    }
    
}
