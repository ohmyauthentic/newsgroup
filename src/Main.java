import java.io.IOException;

/**
 * Created by zmq on 2015/11/18.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        DataPreprocess dataPreprocess = new DataPreprocess("mini_newsgroups");
//        dataPreprocess.readFile();
        dataPreprocess.doProcess();

    }
}
