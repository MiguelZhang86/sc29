package history;

import java.io.*;
import java.time.LocalDateTime;

/**
 * Classe utilitária para escrever entradas com timestamp num ficheiro de log CSV.
 * Cada entrada é prefixada com a data e hora atuais.
 */
public class Log{
    private final String fileName;
    private BufferedWriter writer;

    /**
     * Cria um novo Log, criando o ficheiro e os diretórios necessários.
     * @param fileName caminho para o ficheiro de log
     * @throws IOException se o ficheiro não puder ser criado ou aberto
     */
    public Log(String fileName) throws IOException{
        this.fileName=fileName;
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        this.writer = new BufferedWriter(new FileWriter(file, true));
    }

    /**
     * Escreve uma entrada com timestamp no ficheiro de log.
     * @param value o valor a registar
     * @throws IOException se a escrita falhar
     */
    public void write(String value) throws IOException{
        this.writer.write(LocalDateTime.now() + "," + value);
        this.writer.newLine();
        this.writer.flush();
    }

    /**
     * Fecha o writer subjacente.
     * @throws IOException se o fecho falhar
     */
    public void close() throws IOException{
        this.writer.close();
    }

    /**
     * Retorna o caminho do ficheiro de log.
     * @return o nome/caminho do ficheiro
     */
    public String getFileName(){
        return this.fileName;
    }
}