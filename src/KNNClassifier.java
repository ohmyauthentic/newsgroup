import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**KNN算法的实现类，本程序用向量夹角余弦计算相似度
 */

public class KNNClassifier {
	
	/**用KNN算法对测试文档集分类,读取测试样例和训练样例集
	 * @param trainFiles 训练样例的所有向量构成的文件
	 * @param testFiles 测试样例的所有向量构成的文件
	 * @param kNNResultFile KNN分类结果文件路径
	 * @return double 分类准确率
	 * @throws IOException 
	 */
	private double doProcess(String trainFiles, String testFiles,
			String kNNResultFile) throws IOException {
		// TODO Auto-generated method stub
		//首先读取训练样本和测试样本，用map<String,map<word,TF>>保存测试集和训练集，注意训练样本的类目信息也得保存，
		//然后遍历测试样本，对于每一个测试样本去计算它与所有训练样本的相似度，相似度保存入map<String,double>有
		//序map中去，然后取前K个样本，针对这k个样本来给它们所属的类目计算权重得分，对属于同一个类目的权重求和进而得到
		//最大得分的类目，就可以判断测试样例属于该类目下，K值可以反复测试，找到分类准确率最高的那个值
		//！注意要以"类目_文件名"作为每个文件的key，才能避免同名不同内容的文件出现
		//！本程序用向量夹角余弦计算相似度
		File trainSamples = new File(trainFiles);
		BufferedReader trainSamplesBR = new BufferedReader(new FileReader(trainSamples));
		String line;
		String [] lineSplitBlock;
		Map<String,TreeMap<String,Double>> trainFileNameWordTFMap = new TreeMap<>();
		TreeMap<String,Double> trainWordTFMap = new TreeMap<>();
		while((line = trainSamplesBR.readLine()) != null){
			lineSplitBlock = line.split(" ");
			trainWordTFMap.clear();
			for(int i = 2; i < lineSplitBlock.length; i = i + 2){
				trainWordTFMap.put(lineSplitBlock[i], Double.valueOf(lineSplitBlock[i+1]));
			}
			TreeMap<String,Double> tempMap = new TreeMap<>();
			tempMap.putAll(trainWordTFMap);
			trainFileNameWordTFMap.put(lineSplitBlock[0]+"_"+lineSplitBlock[1], tempMap);
		}
		trainSamplesBR.close();
		
		File testSamples = new File(testFiles);
		BufferedReader testSamplesBR = new BufferedReader(new FileReader(testSamples));
		Map<String,Map<String,Double>> testFileNameWordTFMap = new TreeMap<>();
		Map<String,String> testClassifyCateMap = new TreeMap<>();//分类形成的<文件名，类目>对
		Map<String,Double> testWordTFMap = new TreeMap<>();
		while((line = testSamplesBR.readLine()) != null){
			lineSplitBlock = line.split(" ");
			testWordTFMap.clear();
			for(int i = 2; i < lineSplitBlock.length; i = i + 2){
				testWordTFMap.put(lineSplitBlock[i], Double.valueOf(lineSplitBlock[i+1]));
			}
			TreeMap<String,Double> tempMap = new TreeMap<>();
			tempMap.putAll(testWordTFMap);
			testFileNameWordTFMap.put(lineSplitBlock[0]+"_"+lineSplitBlock[1], tempMap);
		}
		testSamplesBR.close();
		//下面遍历每一个测试样例计算与所有训练样本的距离，做分类
		String classifyResult;
		FileWriter testWriter = new FileWriter(new File("DataMiningSample/docVector/sampleTest"));
		FileWriter KNNClassifyResWriter = new FileWriter(kNNResultFile);
		Set<Map.Entry<String,Map<String,Double>>> testFileNameWordTFMapSet = testFileNameWordTFMap.entrySet();
		for(Iterator<Map.Entry<String,Map<String,Double>>> it = testFileNameWordTFMapSet.iterator(); it.hasNext();){
			Map.Entry<String, Map<String,Double>> me = it.next();
			classifyResult = KNNComputeCate(me.getKey(), me.getValue(), trainFileNameWordTFMap, testWriter);
			KNNClassifyResWriter.append(me.getKey()+" "+classifyResult+"\n");
			KNNClassifyResWriter.flush();
			testClassifyCateMap.put(me.getKey(), classifyResult);
		}
		KNNClassifyResWriter.close();
		//计算分类的准确率
		double righteCount = 0;
		Set<Map.Entry<String, String>> testClassifyCateMapSet = testClassifyCateMap.entrySet();
		for(Iterator <Map.Entry<String, String>> it = testClassifyCateMapSet.iterator(); it.hasNext();){
			Map.Entry<String, String> me = it.next();
			String rightCate = me.getKey().split("_")[0];
			if(me.getValue().equals(rightCate)){
				righteCount++;
			}
		}	
		testWriter.close();
		return righteCount / testClassifyCateMap.size();
	}
	
	/**对于每一个测试样本去计算它与所有训练样本的向量夹角余弦相似度
	 * 相似度保存入map<String,double>有序map中去，然后取前K个样本，
	 * 针对这k个样本来给它们所属的类目计算权重得分，对属于同一个类
	 * 目的权重求和进而得到最大得分的类目，就可以判断测试样例属于该
	 * 类目下。K值可以反复测试，找到分类准确率最高的那个值
	 * @param testWordTFMap 当前测试文件的<单词,词频>向量
	 * @param trainFileNameWordTFMap 训练样本<类目_文件名,向量>Map
	 * @param testYangliuWriter 
	 * @return String K个邻居权重得分最大的类目
	 * @throws IOException 
	 */
	private String KNNComputeCate(
			String testFileName,
			Map<String, Double> testWordTFMap,
			Map<String, TreeMap<String, Double>> trainFileNameWordTFMap, FileWriter testYangliuWriter) throws IOException {
		// TODO Auto-generated method stub
		HashMap<String,Double> simMap = new HashMap<>();//<类目_文件名,距离> 后面需要将该HashMap按照value排序
		double similarity;
		Set<Map.Entry<String,TreeMap<String,Double>>> trainFileNameWordTFMapSet = trainFileNameWordTFMap.entrySet();
		for(Iterator<Map.Entry<String,TreeMap<String,Double>>> it = trainFileNameWordTFMapSet.iterator(); it.hasNext();){
			Map.Entry<String, TreeMap<String,Double>> me = it.next();
			similarity = computeSim(testWordTFMap, me.getValue());
			simMap.put(me.getKey(),similarity);
		}
		//下面对simMap按照value排序
		ByValueComparator bvc = new ByValueComparator(simMap);
		TreeMap<String,Double> sortedSimMap = new TreeMap<>(bvc);
		sortedSimMap.putAll(simMap);
		
		//在disMap中取前K个最近的训练样本对其类别计算距离之和，K的值通过反复试验而得
		Map<String,Double> cateSimMap = new TreeMap<>();//K个最近训练样本所属类目的距离之和
		double K = 20;
		double count = 0;
		double tempSim;
		
		Set<Map.Entry<String, Double>> simMapSet = sortedSimMap.entrySet();
		for(Iterator<Map.Entry<String, Double>> it = simMapSet.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			count++;
			String categoryName = me.getKey().split("_")[0];
			if(cateSimMap.containsKey(categoryName)){
				tempSim = cateSimMap.get(categoryName);
				cateSimMap.put(categoryName, tempSim + me.getValue());
			}
			else cateSimMap.put(categoryName, me.getValue());
			if (count > K) break;
		}
		//下面到cateSimMap里面把sim最大的那个类目名称找出来
		double maxSim = 0;
		String bestCate = null;
		Set<Map.Entry<String, Double>> cateSimMapSet = cateSimMap.entrySet();
		for(Iterator<Map.Entry<String, Double>> it = cateSimMapSet.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			if(me.getValue()> maxSim){
				bestCate = me.getKey();
				maxSim = me.getValue();
			}
		}
		return bestCate;
	}

	/**计算测试样本向量和训练样本向量的相似度
	 * @param testWordTFMap 当前测试文件的<单词,词频>向量
	 * @param trainWordTFMap 当前训练样本<单词,词频>向量
	 * @return Double 向量之间的相似度 以向量夹角余弦计算
	 * @throws IOException 
	 */
	private double computeSim(Map<String, Double> testWordTFMap,
			Map<String, Double> trainWordTFMap) {
		// TODO Auto-generated method stub
		double mul = 0, testAbs = 0, trainAbs = 0;
		Set<Map.Entry<String, Double>> testWordTFMapSet = testWordTFMap.entrySet();
		for(Iterator<Map.Entry<String, Double>> it = testWordTFMapSet.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			if(trainWordTFMap.containsKey(me.getKey())){
				mul += me.getValue()*trainWordTFMap.get(me.getKey());
			}
			testAbs += me.getValue() * me.getValue();
		}
		testAbs = Math.sqrt(testAbs);
		
		Set<Map.Entry<String, Double>> trainWordTFMapSet = trainWordTFMap.entrySet();
		for(Iterator<Map.Entry<String, Double>> it = trainWordTFMapSet.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			trainAbs += me.getValue()*me.getValue();
		}
		trainAbs = Math.sqrt(trainAbs);
		return mul / (testAbs * trainAbs);
	}

	/**根据KNN算法分类结果文件生成正确类目文件，而正确率和混淆矩阵的计算可以复用贝叶斯算法类中的方法
	 * @param kNNRightFile 分类正确类目文件
	 * @param kNNResultFile 分类结果文件
	 * @throws IOException 
	 */
	private void createRightFile(String kNNResultFile, String kNNRightFile) throws IOException {
		// TODO Auto-generated method stub
		String rightCate;
		FileReader fileR = new FileReader(kNNResultFile);
		FileWriter KNNRrightResult = new FileWriter(new File(kNNRightFile));
		BufferedReader fileBR = new BufferedReader(fileR);
		String line;
		String lineBlock[];
		while((line = fileBR.readLine()) != null){
			lineBlock = line.split(" ");
			rightCate = lineBlock[0].split("_")[0];
			KNNRrightResult.append(lineBlock[0]+" "+rightCate+"\n");
		}
		KNNRrightResult.flush();
		KNNRrightResult.close();
	}
		
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public void KNNClassifierMain() throws IOException {
		// TODO Auto-generated method stub
		//wordMap是所有属性词的词典<单词，在所有文档中出现的次数>
		double[] accuracyOfEveryExp = new double[10];
		double accuracyAvg,sum = 0;
		KNNClassifier knnClassifier = new KNNClassifier();
		NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
		Map<String,Double> wordMap = new TreeMap<>();
		Map<String,Double> IDFPerWordMap;
		ComputeWordsVector computeWV = new ComputeWordsVector();
		wordMap = computeWV.countWords("DataMiningSample/SampleWithSpecial", wordMap);
		System.out.println(wordMap.size());
		IDFPerWordMap = computeWV.computeIDF("DataMiningSample/SampleWithSpecial", wordMap);
		System.out.println(IDFPerWordMap.size());
		computeWV.printWordMap(wordMap);
		//首先生成KNN算法10次试验需要的文档TF矩阵文件
		for(int i = 0; i < 10; i++){
			computeWV.computeTFMultiIDF("DataMiningSample/SampleWithSpecial",0.9, i, IDFPerWordMap,wordMap);
			String trainFiles = "DataMiningSample/docVector/wordTFIDFMapTrainSample"+i;
			String testFiles = "DataMiningSample/docVector/wordTFIDFMapTestSample"+i;
			String kNNResultFile = "DataMiningSample/docVector/KNNClassifyResult"+i;
			String kNNRightFile = "DataMiningSample/docVector/KNNClassifyRight"+i;
			accuracyOfEveryExp[i] = knnClassifier.doProcess(trainFiles, testFiles, kNNResultFile);
			knnClassifier.createRightFile(kNNResultFile,kNNRightFile);
			accuracyOfEveryExp[i] = nbClassifier.computeAccuracy(kNNResultFile, kNNRightFile);//计算准确率复用贝叶斯算法中的方法
			sum += accuracyOfEveryExp[i];
			System.out.println("第 "+i+"次 KNN分类器分类的准确率 :" + accuracyOfEveryExp[i]);
		}
		accuracyAvg = sum / 10;
		System.out.println("KNN分类器的总体的平均准确率 :" + accuracyAvg);
	}
	
	//对HashMap按照value做排序
	static class ByValueComparator implements Comparator<Object> {
		HashMap<String, Double> base_map;

		public ByValueComparator(HashMap<String, Double> disMap) {
			this.base_map = disMap;
		}
		
		@Override
		public int compare(Object o1, Object o2) {
			// TODO Auto-generated method stub
			String arg0 = o1.toString();
			String arg1 = o2.toString();
			if (!base_map.containsKey(arg0) || !base_map.containsKey(arg1)) {
				return 0;
			}
			if (base_map.get(arg0) < base_map.get(arg1)) {
				return 1;
			} else if (base_map.get(arg0) == base_map.get(arg1)) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
