package tudbut.mod.client.ttc.utils;

import java.io.*;
import java.net.Socket;

public class Bus {

    InputStream i;
    BufferedReader ip;
    OutputStream o;
    PrintStream op;
    
    public Bus(InputStream i, OutputStream o) {
        if(i != null && o != null) {
            this.i = i;
            this.o = o;
            ip = new BufferedReader(new InputStreamReader(i));
            op = new PrintStream(o);
        }
    }
    public Bus(Socket socket) throws IOException {
        this(socket.getInputStream(), socket.getOutputStream());
    }
    
    public void write(String data) throws IOException {
        op.println(data);
    }
    public void write(int b) throws IOException {
        o.write(b);
    }
    public OutputStream o() {
        return o;
    }
    
    public String  readS() throws IOException {
        return ip.readLine();
    }
    public int read() throws IOException {
        return i.read();
    }
    public InputStream i() {
        return i;
    }
}
