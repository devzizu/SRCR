
package parser.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CSVReader {
    
    public static void csvToProlog(List<File> inputCSVFiles, File prologFile) throws Exception {
 
        HashSet<Integer> paragensGids = new HashSet<>();

        List<String> ligacoes = new ArrayList<>();

        FileOutputStream fos = new FileOutputStream(prologFile);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
        
        boolean isStart = true;
        int r = 0;

        int lastGID = -1, currGID = -1, counter = 1;
        for (File dataSet: inputCSVFiles) {

            counter = 1;
            currGID = lastGID = -1;

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataSet), "UTF8"));
            
            //read first line (format)
            String line = br.readLine();

            if (isStart) {

                //Write line format to the final prolog file
                bw.write("/*");
                bw.newLine();
                bw.write("> Ouput prolog file generated by the parser.");
                bw.newLine();
                bw.write("> CSV files line format:");
                bw.newLine();
                bw.write("\t" + Normalizer.normalize(line, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""));
                bw.newLine();
                bw.write("> Prolog 'paragem' fact:");
                bw.newLine();
                bw.write("\tparagem("+line.replace("Carreira;", "").replace(";",",")+")");
                bw.newLine();
                bw.write("*/");
                bw.newLine();
                bw.newLine();

                bw.write(":- dynamic ligacao/3.");
                bw.newLine();
                bw.write(":- dynamic paragem/10.");
                bw.newLine();
                bw.newLine();
                
                isStart = false;

            }

            while ((line = br.readLine()) != null) {

                line = line.replace("\\s+", "");
                                
                Paragem paragem = new Paragem();
                paragem.loadFromString(line, dataSet.getName());

                if (!paragensGids.contains(paragem.getGid())) {

                    bw.write(paragem.toPrologFact()); 
                    bw.newLine();
                    
                } else {
                    r++;
                }

                paragensGids.add(paragem.getGid());
                counter++;
            
                if (counter > 2) {
                    currGID = paragem.getGid();
                    String arco = "ligacao(" + lastGID + ", " + currGID + ", " + paragem.getCarreira() + ").";
                    ligacoes.add(arco);
                }

                lastGID = paragem.getGid();
            }
    
            br.close();
        }

        bw.newLine();

        for(String s: ligacoes) {
            bw.write(s);
            bw.newLine();
        }

        System.out.println("Repeated GIDs = " + r + ";");
        bw.flush();
        bw.close();

    }
}