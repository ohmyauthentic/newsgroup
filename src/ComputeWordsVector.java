import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;

/**�����ĵ��������������������ĵ�������
 */
public class ComputeWordsVector {
	
	/**�����ĵ���TF��������,ֱ��д�ɶ�ά���������ʽ���ɣ�û��Ҫ�ݹ�
	 * @param strDir ����õ�newsgroup�ļ�Ŀ¼�ľ���·��
	 * @param trainSamplePercent ѵ��������ռÿ����Ŀ�ı���
	 * @param indexOfSample ��������������ʼ�Ĳ����������
	 * @param wordMap ���Դʵ�map
	 * @throws IOException 
	 */
	public void computeTFMultiIDF(String strDir, double trainSamplePercent, int indexOfSample, Map<String, Double> iDFPerWordMap, Map<String, Double> wordMap) throws IOException{
		File fileDir = new File(strDir);
		String word;
		SortedMap<String,Double> TFPerDocMap = new TreeMap<String,Double>();
		//ע�����������д�ļ���һ��ר��д����������һ��ר��дѵ����������sampleType��ֵ����ʾ
		String trainFileDir = "F:/DataMiningSample/docVector/wordTFIDFMapTrainSample"+indexOfSample;
		String testFileDir = "F:/DataMiningSample/docVector/wordTFIDFMapTestSample"+indexOfSample;
		FileWriter tsTrainWriter = new FileWriter(new File(trainFileDir));
		FileWriter tsTestWrtier = new FileWriter(new File(testFileDir));
		FileWriter tsWriter = tsTrainWriter;
		File[] sampleDir = fileDir.listFiles();
		for(int i = 0; i < sampleDir.length; i++){
			String cateShortName = sampleDir[i].getName();
			System.out.println("compute: " + cateShortName);
			File[] sample = sampleDir[i].listFiles();
			double testBeginIndex = indexOfSample*(sample.length * (1-trainSamplePercent));//������������ʼ�ļ����
			double testEndIndex = (indexOfSample+1)*(sample.length * (1-trainSamplePercent));//�����������Ľ����ļ����
			System.out.println("dirName_total length:"+sampleDir[i].getCanonicalPath()+"_"+sample.length);
			System.out.println(trainSamplePercent + " length:"+sample.length * trainSamplePercent +" testBeginIndex:"+testBeginIndex+" testEndIndex"+ testEndIndex);	
			for(int j = 0;j < sample.length; j++){
				TFPerDocMap.clear();
				FileReader samReader = new FileReader(sample[j]);
				BufferedReader samBR = new BufferedReader(samReader);
				String fileShortName = sample[j].getName();
				Double wordSumPerDoc = 0.0;//����ÿƪ�ĵ����ܴ���
				while((word = samBR.readLine()) != null){
					if(!word.isEmpty() && wordMap.containsKey(word)){//���������Դʵ�����Ĵʣ�ȥ���Ĵʲ�����
						wordSumPerDoc++;
						if(TFPerDocMap.containsKey(word)){
							Double count =  TFPerDocMap.get(word);
							TFPerDocMap.put(word, count + 1);
						}
						else {
							TFPerDocMap.put(word, 1.0);
						}
					}
				}
				//����һ�µ�ǰ�ĵ���TFmap�������ĵ����ܴ������ɴ�Ƶ,Ȼ�󽫴�Ƶ���Դʵ�IDF���õ����յ�����Ȩֵ������������ļ�
				//ע�����������ѵ������д����ļ���ͬ
				if(j >= testBeginIndex && j <= testEndIndex){
					tsWriter = tsTestWrtier;
				}
				else{
					tsWriter = tsTrainWriter;
				}
				Double wordWeight;
				Set<Map.Entry<String, Double>> tempTF = TFPerDocMap.entrySet();
				for(Iterator<Map.Entry<String, Double>> mt = tempTF.iterator(); mt.hasNext();){
					Map.Entry<String, Double> me = mt.next();
					//wordWeight =  (me.getValue() / wordSumPerDoc) * IDFPerWordMap.get(me.getKey());
					//���ڼ���IDF�ǳ���ʱ��3�����ʵ����Դʵ����������Ҫ25��Сʱ���ȳ�����Ϊ���дʵ�IDF����1�����
					wordWeight =  (me.getValue() / wordSumPerDoc) * 1.0;
					TFPerDocMap.put(me.getKey(), wordWeight);
				}
				tsWriter.append(cateShortName + " ");
				String keyWord = fileShortName.substring(0,5);
				tsWriter.append(keyWord+ " ");
				Set<Map.Entry<String, Double>> tempTF2 = TFPerDocMap.entrySet();
				for(Iterator<Map.Entry<String, Double>> mt = tempTF2.iterator(); mt.hasNext();){
					Map.Entry<String, Double> ne = mt.next();
					tsWriter.append(ne.getKey() + " " + ne.getValue() + " ");
				}
				tsWriter.append("\n");	
				tsWriter.flush();
			}
		}
		tsTrainWriter.close();
		tsTestWrtier.close();
		tsWriter.close();
	}
	
	/**ͳ��ÿ���ʵ��ܵĳ��ִ��������س��ִ�������3�εĴʻ㹹�����յ����Դʵ�
	 * @param strDir ����õ�newsgroup�ļ�Ŀ¼�ľ���·��
	 * @throws IOException 
	 */
	public SortedMap<String,Double> countWords(String strDir,Map<String, Double> wordMap) throws IOException{
		File sampleFile = new File(strDir);
		File [] sample = sampleFile.listFiles();
		String word;
		for(int i = 0; i < sample.length; i++){
			if(!sample[i].isDirectory()){
				if(sample[i].getName().contains("stemed")){
					FileReader samReader = new FileReader(sample[i]);
					BufferedReader samBR = new BufferedReader(samReader);
					while((word = samBR.readLine()) != null){
						if(!word.isEmpty() && wordMap.containsKey(word)){
							double count = wordMap.get(word) + 1;
							wordMap.put(word, count);
						}
						else {
							wordMap.put(word, 1.0);
						}
					}
				}	
			}
			else countWords(sample[i].getCanonicalPath(),wordMap);
		}
		//ֻ���س��ִ�������3�ĵ���
		SortedMap<String,Double> newWordMap = new TreeMap<String,Double>();
		Set<Map.Entry<String,Double>> allWords = wordMap.entrySet();
		for(Iterator<Map.Entry<String,Double>> it = allWords.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			if(me.getValue() >= 4){
				newWordMap.put(me.getKey(),me.getValue());
			}
		}
		return newWordMap;	
	}
	
	/**��ӡ���Դʵ�
	 * @param SortedMap<String,Double> ���Դʵ�
	 * @throws IOException 
	 */
	void printWordMap(Map<String, Double> wordMap) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("printWordMap");
		int countLine = 0;
		File outPutFile = new File("F:/DataMiningSample/docVector/allDicWordCountMap.txt");
		FileWriter outPutFileWriter = new FileWriter(outPutFile);
		Set<Map.Entry<String,Double>> allWords = wordMap.entrySet();
		for(Iterator<Map.Entry<String,Double>> it = allWords.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			outPutFileWriter.write(me.getKey()+" "+me.getValue()+"\n");
			countLine++;
		}
		System.out.println("WordMap size" + countLine);
	}
	
	/**����IDF�������Դʵ���ÿ�����ڶ��ٸ��ĵ��г��ֹ�
	 * @param SortedMap<String,Double> ���Դʵ�
	 * @return ���ʵ�IDFmap
	 * @throws IOException 
	 */
	SortedMap<String,Double> computeIDF(String string, Map<String, Double> wordMap) throws IOException {
		// TODO Auto-generated method stub
		File fileDir = new File(string);
		String word;
		SortedMap<String,Double> IDFPerWordMap = new TreeMap<String,Double>();	
		Set<Map.Entry<String, Double>> wordMapSet = wordMap.entrySet();
		for(Iterator<Map.Entry<String, Double>> pt = wordMapSet.iterator(); pt.hasNext();){
			Map.Entry<String, Double> pe = pt.next();
			Double coutDoc = 0.0;
			String dicWord = pe.getKey();
			File[] sampleDir = fileDir.listFiles();
			for(int i = 0; i < sampleDir.length; i++){
				File[] sample = sampleDir[i].listFiles();
				for(int j = 0;j < sample.length; j++){
					FileReader samReader = new FileReader(sample[j]);
					BufferedReader samBR = new BufferedReader(samReader);
					boolean isExited = false;
					while((word = samBR.readLine()) != null){
						if(!word.isEmpty() && word.equals(dicWord)){
							isExited = true;
							break;
						}
					}
					if(isExited) coutDoc++;	
					}	
				}
			//���㵥�ʵ�IDF
			Double IDF = Math.log(20000 / coutDoc) / Math.log(10);
			IDFPerWordMap.put(dicWord, IDF);
			}
		return IDFPerWordMap;
	}
}
