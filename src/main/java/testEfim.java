
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import efim.AlgoEFIM;
import efim.Itemsets;

/**
 * Created by juanfranfv on 7/1/17.
 */
public class testEfim {
    public static void main(String [] arg) throws IOException {

        // the input and output file paths
        String input = fileToPath("DB_Utility2.txt");

        // the minutil threshold
        int minutil = 30;

        // Run the EFIM algorithm
        AlgoEFIM algo = new AlgoEFIM();
        Itemsets itemsets = algo.runAlgorithm(minutil,  input, null, true, Integer.MAX_VALUE, true);
        // Print statistics
        algo.printStats();

        // Print the itemsets
        itemsets.printItemsets();
    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = testEfim.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
}
