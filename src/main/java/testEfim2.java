import efim.AlgoEFIM3;
import efim.AlgoEFIM2;
import efim.AlgoEFIM0;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Created by juanfranfv on 7/1/17.
 */
public class testEfim2 implements Serializable{
    public static void main(String [] arg) throws IOException {

        // the input and output file paths
        String input;
        String output = ".//output";
        if(arg.length > 0){
            input = arg[0];
            if(arg.length > 1){
                output = arg[1];
            }
        }else {
            input = fileToPath("datasets/chess_utility_spmf.txt");
        }


        // the minutil threshold
        int minutil = 366632;
        double tetha = 0.3;

        // Run the EFIM algorithm
        AlgoEFIM0 algo = new AlgoEFIM0();
        algo.runAlgorithm(minutil,  input, output, true, Integer.MAX_VALUE, true);
        // Print statistics
        algo.printStats();
//        while (true) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = testEfim2.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
}
