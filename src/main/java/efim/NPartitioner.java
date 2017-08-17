package efim;

import org.apache.spark.Partitioner;

/**
 * Created by juanfranfv on 8/16/17.
 */
public class NPartitioner extends Partitioner {
    private int numParts;

    public NPartitioner(int i) {
        numParts=i;
    }

    @Override
    public int numPartitions()
    {
        return numParts;
    }

    @Override
    public int getPartition(Object key){
        Integer clave = (Integer)key;
        Integer resto = clave%numParts;
        Integer dividendo = clave/numParts;
        if(resto == 0)
        {
            if((dividendo & 1) == 0) {
                return resto;
            }
            return  (numParts-1);
        }else if((dividendo & 1) == 0)
        {
            //par
            return (resto-1);
        }
        int calculo =  numParts - resto;
        return calculo;

    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof NPartitioner)
        {
            NPartitioner partitionerObject = (NPartitioner)obj;
            if(partitionerObject.numParts == this.numParts)
                return true;
        }

        return false;
    }
}

