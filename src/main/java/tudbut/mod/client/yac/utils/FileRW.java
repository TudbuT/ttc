package tudbut.mod.client.yac.utils;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class FileRW {
    
    private final ArrayList<String> lines;
    protected File file;
    
    public FileRW(String path) throws IOException {
        
        this.file = new File(path);
        
        if (!this.file.exists()) {
            this.file.createNewFile();
            new BufferedWriter(new FileWriter(this.file)).write("\n");
        }
        this.lines = new ArrayList<>();
        rereadFile();
    }
    
    public String getContent() {
        StringBuilder builder = new StringBuilder();
        for (String line : lines)
            builder.append(line);
        return builder.toString();
    }
    
    public void setContent(String content) throws IOException {
        this.lines.clear();
        this.lines.addAll(Arrays.asList(content.split("\n")));
        FileOutputStream fileWriter = new FileOutputStream(this.file);
        PrintWriter writer = new PrintWriter(fileWriter);
        writer.write(content);
        writer.close();
    }
    
    public void rereadFile() throws IOException {
        BufferedReader stream = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        lines.clear();
        String line;
        while ((line = stream.readLine()) != null) {
            for (int i = 0; i < line.length(); i++) {
                lines.add(line);
            }
            lines.add("\n");
        }
    }
}