import java.io.IOException;

/**
 * Created by zmq on 2015/11/18.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String sampleDir = "mini_newsgroups";
//        DataPreprocess dataPreprocess = new DataPreprocess(sampleDir);
//        dataPreprocess.doProcess();
//        NaiveBayesianClassifier naiveBayesianClassifier = new NaiveBayesianClassifier();
//        naiveBayesianClassifier.NaiveBayesianClassifierMain();
        KNNClassifier knnClassifier = new KNNClassifier();
        knnClassifier.KNNClassifierMain();
    }
}
