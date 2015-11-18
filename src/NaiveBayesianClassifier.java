import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**�������ر�Ҷ˹�㷨��newsgroup�ĵ��������࣬����ʮ�齻�����ȡƽ��ֵ
 * ���ö���ʽģ��,stanford��Ϣ�������ۿμ������Զ���ʽģ�ͱȲ�Ŭ��ģ��׼ȷ�ȸ�
 * ����������P(tk|c)=(��c �µ���tk �ڸ����ĵ��г��ֹ��Ĵ���֮��+1)/(��c�µ�������+|V|)
 */
public class NaiveBayesianClassifier {
	
	/**�ñ�Ҷ˹���Բ����ĵ�������
	 * @param trainDir ѵ���ĵ���Ŀ¼
	 * @param testDir �����ĵ���Ŀ¼
	 * @param classifyResultFileNew �������ļ�·��
	 * @throws Exception 
	 */
	private void doProcess(String trainDir, String testDir,
			String classifyResultFileNew) throws Exception {
		// TODO Auto-generated method stub
		Map<String,Double> cateWordsNum = new TreeMap<String,Double>();//����ѵ����ÿ�������ܴ���
		Map<String,Double> cateWordsProb = new TreeMap<String,Double>();//����ѵ������ÿ�������ÿ�����Դʵĳ��ִ���
		cateWordsProb = getCateWordsProb(trainDir);
		cateWordsNum = getCateWordsNum(trainDir);
		double totalWordsNum = 0.0;//��¼����ѵ�������ܴ���
		Set<Map.Entry<String,Double>> cateWordsNumSet = cateWordsNum.entrySet();
		for(Iterator<Map.Entry<String,Double>> it = cateWordsNumSet.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			totalWordsNum += me.getValue();
		}
		//���濪ʼ��ȡ��������������
		Vector<String> testFileWords = new Vector<String>();
		String word;
		File[] testDirFiles = new File(testDir).listFiles();
		FileWriter crWriter = new FileWriter(classifyResultFileNew);
		for(int i = 0; i < testDirFiles.length; i++){
			File[] testSample = testDirFiles[i].listFiles();
			for(int j = 0;j < testSample.length; j++){
				testFileWords.clear();
				FileReader spReader = new FileReader(testSample[j]);
				BufferedReader spBR = new BufferedReader(spReader);
				while((word = spBR.readLine()) != null){
					testFileWords.add(word);
				}
				//����ֱ����ò����������ڶ�ʮ�����ĸ���
				File[] trainDirFiles = new File(trainDir).listFiles();
				BigDecimal maxP = new BigDecimal(0);
				String bestCate = null;
				for(int k = 0; k < trainDirFiles.length; k++){
					BigDecimal p = computeCateProb(trainDirFiles[k], testFileWords, cateWordsNum, totalWordsNum, cateWordsProb);
					if(k == 0){
						maxP = p;
						bestCate = trainDirFiles[k].getName();
						continue;
					}
					if(p.compareTo(maxP) == 1){
						maxP = p;
						bestCate = trainDirFiles[k].getName();
					}
				}
				crWriter.append(testSample[j].getName() + " " + bestCate + "\n");
				crWriter.flush();
			}
		}
		crWriter.close();
	}
	
	/**ͳ��ĳ��ѵ��������ÿ�����ʵĳ��ִ���
	 * @param strDir ѵ��������Ŀ¼
	 * @return Map<String,Double> cateWordsProb ��"��Ŀ_����"����������map,�����val���Ǹ���Ŀ�¸õ��ʵĳ��ִ���
	 * @throws IOException 
	 */
	public Map<String,Double> getCateWordsProb(String strDir) throws IOException{
		Map<String,Double> cateWordsProb = new TreeMap<String,Double>();
		File sampleFile = new File(strDir);
		File [] sampleDir = sampleFile.listFiles();
		String word;
		for(int i = 0;i < sampleDir.length; i++){
			File [] sample = sampleDir[i].listFiles();
			for(int j = 0; j < sample.length; j++){
				FileReader samReader = new FileReader(sample[j]);
				BufferedReader samBR = new BufferedReader(samReader);
				while((word = samBR.readLine()) != null){
					String key = sampleDir[i].getName() + "_" + word;
					if(cateWordsProb.containsKey(key)){
						double count = cateWordsProb.get(key) + 1.0;
						cateWordsProb.put(key, count);
					}
					else {
						cateWordsProb.put(key, 1.0);
					}
				}
			}
		}
		return cateWordsProb;	
	}
	
	/**����ĳһ��������������ĳ�����ĸ���
	 * @param Map<String, Double> cateWordsProb ��¼ÿ��Ŀ¼�г��ֵĵ��ʼ����� 
	 * @param File trainFile ��������е�ѵ����������Ŀ¼
	 * @param Vector<String> testFileWords �ò��������е����дʹ��ɵ�����
	 * @param double totalWordsNum ��¼����ѵ�������ĵ�������
	 * @param Map<String, Double> cateWordsNum ��¼ÿ�����ĵ�������
	 * @return BigDecimal ���ظò��������ڸ�����еĸ���
	 * @throws Exception 
	 * @throws IOException 
	 */
	private BigDecimal computeCateProb(File trainFile, Vector<String> testFileWords, Map<String, Double> cateWordsNum, double totalWordsNum, Map<String, Double> cateWordsProb) throws Exception {
		// TODO Auto-generated method stub
		BigDecimal probability = new BigDecimal(1);
		double wordNumInCate = cateWordsNum.get(trainFile.getName());
		BigDecimal wordNumInCateBD = new BigDecimal(wordNumInCate);
		BigDecimal totalWordsNumBD = new BigDecimal(totalWordsNum);
		for(Iterator<String> it = testFileWords.iterator(); it.hasNext();){
			String me = it.next();
			String key = trainFile.getName()+"_"+me;
			double testFileWordNumInCate;
			if(cateWordsProb.containsKey(key)){
				testFileWordNumInCate = cateWordsProb.get(key);
			}else testFileWordNumInCate = 0.0;
			BigDecimal testFileWordNumInCateBD = new BigDecimal(testFileWordNumInCate);
			BigDecimal xcProb = (testFileWordNumInCateBD.add(new BigDecimal(1))).divide(totalWordsNumBD.add(wordNumInCateBD),10, BigDecimal.ROUND_CEILING);
			probability = probability.multiply(xcProb);
		}
		BigDecimal res = probability.multiply(wordNumInCateBD.divide(totalWordsNumBD,10, BigDecimal.ROUND_CEILING));
		return res;
	}

	/**���ÿ����Ŀ�µĵ�������
	 * @param trainDir ѵ���ĵ���Ŀ¼
	 * @return Map<String, Double> <Ŀ¼������������>��map
	 * @throws IOException 
	 */
	private Map<String, Double> getCateWordsNum(String trainDir) throws IOException {
		// TODO Auto-generated method stub
		Map<String,Double> cateWordsNum = new TreeMap<String,Double>();
		File[] sampleDir = new File(trainDir).listFiles();
		for(int i = 0; i < sampleDir.length; i++){
			double count = 0;
			File[] sample = sampleDir[i].listFiles();
			for(int j = 0;j < sample.length; j++){
				FileReader spReader = new FileReader(sample[j]);
				BufferedReader spBR = new BufferedReader(spReader);
				while(spBR.readLine() != null){
					count++;
				}		
			}
			cateWordsNum.put(sampleDir[i].getName(), count);
		}
		return cateWordsNum;
	}
	
	/**������ȷ��Ŀ�ļ��ͷ������ļ�ͳ�Ƴ�׼ȷ��
	 * @param classifyResultFile ��ȷ��Ŀ�ļ�
	 * @param classifyResultFileNew �������ļ�
	 * @return double �����׼ȷ��
	 * @throws IOException 
	 */
	double computeAccuracy(String classifyResultFile,
			String classifyResultFileNew) throws IOException {
		// TODO Auto-generated method stub
		Map<String,String> rightCate = new TreeMap<String,String>();
		Map<String,String> resultCate = new TreeMap<String,String>();
		rightCate = getMapFromResultFile(classifyResultFile);
		resultCate = getMapFromResultFile(classifyResultFileNew);
		Set<Map.Entry<String, String>> resCateSet = resultCate.entrySet();
		double rightCount = 0.0;
		for(Iterator<Map.Entry<String, String>> it = resCateSet.iterator(); it.hasNext();){
			Map.Entry<String, String> me = it.next();
			if(me.getValue().equals(rightCate.get(me.getKey()))){
				rightCount ++;
			}
		}
		computerConfusionMatrix(rightCate,resultCate);
		return rightCount / resultCate.size();	
	}
	
	/**������ȷ��Ŀ�ļ��ͷ������ļ���������������
	 * @param rightCate ��ȷ��Ŀ��Ӧmap
	 * @param resultCate ��������Ӧmap
	 * @return double �����׼ȷ��
	 * @throws IOException 
	 */
	private void computerConfusionMatrix(Map<String, String> rightCate,
			Map<String, String> resultCate) {
		// TODO Auto-generated method stub	
		int[][] confusionMatrix = new int[20][20];
		//���������Ŀ��Ӧ����������
		SortedSet<String> cateNames = new TreeSet<String>();
		Set<Map.Entry<String, String>> rightCateSet = rightCate.entrySet();
		for(Iterator<Map.Entry<String, String>> it = rightCateSet.iterator(); it.hasNext();){
			Map.Entry<String, String> me = it.next();
			cateNames.add(me.getValue());
		}
		cateNames.add("rec.sport.baseball");//��ֹ����һ����Ŀ
		String[] cateNamesArray = cateNames.toArray(new String[0]);
		Map<String,Integer> cateNamesToIndex = new TreeMap<String,Integer>();
		for(int i = 0; i < cateNamesArray.length; i++){
			cateNamesToIndex.put(cateNamesArray[i],i);
		}
		for(Iterator<Map.Entry<String, String>> it = rightCateSet.iterator(); it.hasNext();){
			Map.Entry<String, String> me = it.next();
			confusionMatrix[cateNamesToIndex.get(me.getValue())][cateNamesToIndex.get(resultCate.get(me.getKey()))]++;
		}
		//�����������
		double[] hangSum = new double[20];
		System.out.print("    ");
		for(int i = 0; i < 20; i++){
			System.out.print(i + "    ");
		}
		System.out.println();
		for(int i = 0; i < 20; i++){
			System.out.print(i + "    ");
			for(int j = 0; j < 20; j++){
				System.out.print(confusionMatrix[i][j]+"    ");
				hangSum[i] += confusionMatrix[i][j];
			}
			System.out.println(confusionMatrix[i][i] / hangSum[i]);
		}
		System.out.println();
	}

	/**�ӷ������ļ��ж�ȡmap
	 * @param classifyResultFileNew ��Ŀ�ļ�
	 * @return Map<String, String> ��<�ļ�������Ŀ��>�����map
	 * @throws IOException 
	 */
	private Map<String, String> getMapFromResultFile(
			String classifyResultFileNew) throws IOException {
		// TODO Auto-generated method stub
		File crFile = new File(classifyResultFileNew);
		FileReader crReader = new FileReader(crFile);
		BufferedReader crBR = new BufferedReader(crReader);
		Map<String, String> res = new TreeMap<String, String>();
		String[] s;
		String line;
		while((line = crBR.readLine()) != null){
			s = line.split(" ");
			res.put(s[0], s[1]);	
		}
		return res;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public void NaiveBayesianClassifierMain(String[] args) throws Exception {
		 //TODO Auto-generated method stub
		//���ȴ���ѵ�����Ͳ��Լ�
		CreateTrainAndTestSample ctt = new CreateTrainAndTestSample();
		NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
		ctt.filterSpecialWords();//���ݰ����������ʵ��ĵ�������ֻ���������ʵ��ĵ�����processedSampleOnlySpecialĿ¼��
		double[] accuracyOfEveryExp = new double[10];
		double accuracyAvg,sum = 0;
		for(int i = 0; i < 10; i++){//�ý�����֤����ʮ�η���ʵ�飬��׼ȷ��ȡƽ��ֵ	
			String TrainDir = "F:/DataMiningSample/TrainSample"+i;
			String TestDir = "F:/DataMiningSample/TestSample"+i;
			String classifyRightCate = "F:/DataMiningSample/classifyRightCate"+i+".txt";
			String classifyResultFileNew = "F:/DataMiningSample/classifyResultNew"+i+".txt";
			ctt.createTestSamples("F:/DataMiningSample/processedSampleOnlySpecial", 0.9, i,classifyRightCate);
			nbClassifier.doProcess(TrainDir,TestDir,classifyResultFileNew);
			accuracyOfEveryExp[i] = nbClassifier.computeAccuracy (classifyRightCate, classifyResultFileNew);
			System.out.println("The accuracy for Naive Bayesian Classifier in "+i+"th Exp is :" + accuracyOfEveryExp[i]);
		}
		for(int i = 0; i < 10; i++){
			sum += accuracyOfEveryExp[i];
		}
		accuracyAvg = sum / 10;
		System.out.println("The average accuracy for Naive Bayesian Classifier in all Exps is :" + accuracyAvg);
		
	}
}
