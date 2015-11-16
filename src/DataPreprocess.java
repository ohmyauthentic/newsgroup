


import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
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

    private void Analyzer(String testString){
        List<String> list = new ArrayList<>();
        try{
            StringReader reader = new StringReader(testString);
            FileReader stopWords = new FileReader("./resource/stopword.txt");
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_20,stopWords);
            TokenStream ts = analyzer.tokenStream("", reader);
            ts.addAttribute(CharTermAttribute.class);
            while (ts.incrementToken()) {
                CharTermAttribute charTermAttribute = ts.getAttribute(CharTermAttribute.class);
                String word = charTermAttribute.toString();
                list.add(word);
                System.out.println(charTermAttribute.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
