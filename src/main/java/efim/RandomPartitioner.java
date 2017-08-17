package efim;

import org.apache.spark.Partitioner;

import java.util.Random;

/**
 * Created by juanfranfv on 8/16/17.
 */
public class RandomPartitioner extends Partitioner {
    private int numParts;

    public RandomPartitioner(int i) {
        numParts=i;
    }

    @Override
    public int numPartitions()
    {
        return numParts;
    }

    @Override
    public int getPartition(Object key){
        Random rand = new Random();
        return rand.nextInt(numParts);
        //partition based on the first character of the key...you can have your logic here !!
        //return ((String)key).charAt(0)%numParts;

    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof RandomPartitioner)
        {
            RandomPartitioner partitionerObject = (RandomPartitioner)obj;
            if(partitionerObject.numParts == this.numParts)
                return true;
        }

        return false;
    }
}

