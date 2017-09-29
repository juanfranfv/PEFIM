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
            input = fileToPath("500.txt");
//            input = fileToPath("accidents.txt");
//            input = "https://s3.us-east-2.amazonaws.com/pefim/chess.txt";
        }


        // the minutil threshold
        //minutil accidents
        //int minutil = 25000000;
        //min util chess
        int minutil = 10000;
        double tetha = 0.3;

        // Run the EFIM algorithm
        AlgoEFIM0 algo = new AlgoEFIM0();
        algo.runAlgorithm(tetha,  input, output, true, Integer.MAX_VALUE, true);
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
