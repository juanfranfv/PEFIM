package efim;

import java.io.Serializable;
import java.util.List;

/**
 * Created by juanfranfv on 8/3/17.
 */
public class HUI implements Serializable {
    private Itemset itemset;
    private int level;

    public HUI(Itemset itemset, int level) {
        this.itemset = itemset;
        this.level = level;
    }

    public Itemset getItemset(){
        return this.itemset;
    }

    public int getLevel(){
        return this.level;
    }
}
