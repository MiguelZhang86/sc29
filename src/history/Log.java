package history;

import java.io.*;
import java.time.LocalDateTime;

public class Log{
    private final String fileName;
    private BufferedWriter writer;

    public Log(String fileName) throws IOException{
        this.fileName=fileName;
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        this.writer = new BufferedWriter(new FileWriter(file, true));
    }

    public void write(String value) throws IOException{
        this.writer.write(LocalDateTime.now() + "," + value);
        this.writer.newLine();
        this.writer.flush();
    }

    public void close() throws IOException{
        this.writer.close();
    }

    public String getFileName(){
        return this.fileName;
    }
}