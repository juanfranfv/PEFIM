package efim;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SparkSession;


/**
 * Created by juanfranfv on 7/14/17.
 */
public class SparkConnection {
    //A name for the spark instance. Can be any string
    private static String appName = "EFIM";
    //Pointer / URL to the Spark instance - embedded
    private static String sparkMaster = "local[2]";

    private static JavaSparkContext spContext = null;
    private static SparkContext sc = null;
    private static SparkSession sparkSession = null;
    private static String tempDir = "file:/Users/juanfranfv/IdeaProjects/sparkapp/spark-warehouse";

    private static void getConnection() {

        if ( spContext == null) {
            //Setup Spark configuration
            SparkConf conf = new SparkConf()
                    .setAppName(appName);
//                    .setMaster(sparkMaster);

            //Make sure you download the winutils binaries into this directory
            //from https://github.com/srccodes/hadoop-common-2.2.0-bin/archive/master.zip
            System.setProperty("hadoop.home.dir", "/");

            //Create Spark Context from configuration
            sc = new SparkContext(conf);
            spContext = JavaSparkContext.fromSparkContext(sc);

            sparkSession = SparkSession
                    .builder()
                    .appName(appName)
//                    .master(sparkMaster)
//                    .config("spark.sql.warehouse.dir", tempDir)
                    .getOrCreate();

            spContext =  new JavaSparkContext(sparkSession.sparkContext());

        }

    }

    public static JavaSparkContext getContext() {

        if ( spContext == null ) {
            getConnection();
        }
        return spContext;
    }

    public static SparkSession getSession() {
        if ( sparkSession == null) {
            getConnection();
        }
        return sparkSession;
    }

    public static void stopSpark(){
        sparkSession.stop();
    }

}