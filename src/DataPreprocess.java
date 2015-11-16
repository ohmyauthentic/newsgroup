import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
/**
 * Created by candy on 2015/11/15.
 * 读取数据并且分词
 */
public class DataPreprocess {
    private String filename;
    private File dir;
    public DataPreprocess(String filename) {
        this.filename = filename;
        dir = new File(filename);
    }

    /**
     * 直接对文件进行分词
     * @param file
     * @return
     * @throws IOException
     */
    private static List Analyzer(File file)throws IOException{
        List<String> list = new ArrayList<>();
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(file));
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            TokenStream ts = analyzer.tokenStream("", reader);
            ts.addAttribute(CharTermAttribute.class);
            while (ts.incrementToken()) {
                CharTermAttribute charTermAttribute = ts.getAttribute(CharTermAttribute.class);
                String word = charTermAttribute.toString();
                list.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 读文件
     * @return
     * @throws IOException
     */
    public Map readFile() throws IOException {
        Map<List,String> map = new HashMap<>();
        File[] files = dir.listFiles();
        for(File file:files){
            String classfiction = file.getName();
            System.out.println(classfiction);
            File[] texts = file.listFiles();
            for(File text: texts){
                List listWords = Analyzer(text);
                if(!listWords.isEmpty()){
                    map.put(listWords,classfiction);
                }
            }
        }
        return map;
    }

    /**
     *特征词提取
     * @param records
     * @return
     */
    public List getSpecificWord(Map records){
        return null;
    }

    public static void main(String[] args) throws IOException {
        DataPreprocess dataPreprocess = new DataPreprocess("mini_newsgroups");
        dataPreprocess.readFile();
    }
}
