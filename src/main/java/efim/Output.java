package efim;

import java.io.Serializable;

/**
 * Created by juanfranfv on 8/3/17.
 */
public class Output implements Serializable {
    public int prefix;
    public int utility;
    public int e;
    public int[] copy;

    public Output(int prefix, int utility, int e, int[] copy) {
        this.prefix = prefix;
        this.utility = utility;
        this.e = e;
        this.copy = copy;
    }

    public String toString(){
        StringBuffer r = new StringBuffer();
        r.append(utility);
        return r.toString();
    }


}
