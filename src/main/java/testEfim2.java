import efim.AlgoEFIM2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Created by juanfranfv on 7/1/17.
 */
public class testEfim2 {
    public static void main(String [] arg) throws IOException {

        // the input and output file paths
        String input = fileToPath("db with utility.txt");

        // the minutil threshold
        int minutil = 100;

        // Run the EFIM algorithm
        AlgoEFIM2 algo = new AlgoEFIM2();
        algo.runAlgorithm(minutil,  input, null, true, Integer.MAX_VALUE, true);
        // Print statistics
        algo.printStats();

    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = testEfim2.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
}
