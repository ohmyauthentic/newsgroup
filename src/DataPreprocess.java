import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by candy on 2015/11/15.
 */
public class DataPreprocess {
    private String filename;
    private File dir;
    public DataPreprocess(String filename) {
        this.filename = filename;
        dir = new File(filename);
    }


    public static StringBuilder readFileByLines(File file) {
        StringBuilder s = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                s.append(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return s;
    }

    public List<String> readFile(){
        List<String> list = new ArrayList<>();

        File[] files = dir.listFiles();
        for(File file:files){
            String classfiction = file.getName();
            System.out.println(classfiction);
            File[] texts = file.listFiles();
            for(File text: texts){

                StringBuilder s = readFileByLines(text);
                if(s!=null){
                    list.add(s.toString());
                }
            }
        }
        return list;
    }

    public static void main(String[] args){
        DataPreprocess dataPreprocess = new DataPreprocess("mini_newsgroups");
        dataPreprocess.readFile();
    }
}
