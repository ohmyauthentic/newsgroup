import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
/**
 * Created by zmq on 2015/11/15.
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
        File stopWordFile = new File("stopwords.txt");
        FileReader stopReader = new FileReader(stopWordFile);
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(file));
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36,stopReader);
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


    public void doProcess() throws IOException{
        String dirTarget = "new_mini_newsgroups";
        File fileTarget = new File(dirTarget);
        if(!fileTarget.exists()){//注意processedSample需要先建立目录建出来，否则会报错，因为母目录不存在
            fileTarget.mkdir();
        }
        File[] files = dir.listFiles();
        String[] stemFileNames = new String[files.length];
        List<String> allFiles = new ArrayList<>();
        for(File file:files){
            String classfiction = file.getName();
            System.out.println(classfiction);
            File[] texts = file.listFiles();
            for(File text: texts){
                String targetPath = dirTarget + "/" + classfiction;
                File newFileTarget = new File(targetPath);
                if(!newFileTarget.exists()){//注意processedSample需要先建立目录建出来，否则会报错，因为母目录不存在
                    newFileTarget.mkdir();
                }
                allFiles.add(targetPath+"/"+text.getName());
                createProcessFile(text.getPath(), targetPath + "/"+text.getName());
            }
        }
        //调用stemmer 提取词干
//        if(allFiles.size()>0){
//            System.out.println(allFiles.toString());
//            Stemmer.porterMain(allFiles);
//        }
    }

    private static void createProcessFile(String srcDir, String targetDir) throws IOException {
        // TODO Auto-generated method stub
        FileReader srcFileReader = new FileReader(srcDir);
        FileReader stopWordsReader = new FileReader("stopwords.txt");
        FileWriter targetFileWriter = new FileWriter(targetDir);
        BufferedReader srcFileBR = new BufferedReader(srcFileReader);//装饰模式
        BufferedReader stopWordsBR = new BufferedReader(stopWordsReader);
        String line, resLine, stopWordsLine;
        //用stopWordsBR够着停用词的ArrayList容器
        ArrayList<String> stopWordsArray = new ArrayList<String>();
        while((stopWordsLine = stopWordsBR.readLine()) != null){
            if(!stopWordsLine.isEmpty()){
                stopWordsArray.add(stopWordsLine);
            }
        }
        while((line = srcFileBR.readLine()) != null){
            resLine = lineProcess(line,stopWordsArray);
            if(!resLine.isEmpty()){
                //按行写，一行写一个单词
                String[] tempStr = resLine.split(" ");//\s
                for(int i = 0; i < tempStr.length; i++){
                    if(!tempStr[i].isEmpty()){
                        targetFileWriter.append(tempStr[i]+"\n");
                    }
                }
            }
        }
        targetFileWriter.flush();
        targetFileWriter.close();
        srcFileReader.close();
        stopWordsReader.close();
        srcFileBR.close();
        stopWordsBR.close();
    }


    /**对每行字符串进行处理，主要是词法分析、去停用词和stemming
     * @param line 待处理的一行字符串
     * @param ArrayList<String> 停用词数组
     * @return String 处理好的一行字符串，是由处理好的单词重新生成，以空格为分隔符
     * @throws IOException
     */
    private static String lineProcess(String line, ArrayList<String> stopWordsArray) throws IOException {
        // TODO Auto-generated method stub
        //step1 英文词法分析，去除数字、连字符、标点符号、特殊字符，所有大写字母转换成小写，可以考虑用正则表达式
        String res[] = line.split("[^a-zA-Z]");
        String resString = new String();
        //step2去停用词
        //step3stemming,返回后一起做
        for(int i = 0; i < res.length; i++){
            if(!res[i].isEmpty() && !stopWordsArray.contains(res[i].toLowerCase())){
                resString += " " + res[i].toLowerCase() + " ";
            }
        }
        return resString;
    }

}
