package efim;

/**
 * Created by juanfranfv on 8/2/17.
 */

import java.io.Serializable;
import java.util.List;


public class ItemTransactions implements Serializable {
    public List<Transaction> transactions;
    public int item;

    public void setItem(int i){
        this.item = i;
    }

    public int getItem(){
        return this.item;
    }

    public void setTransactions(List<Transaction> ts){
        this.transactions = ts;
    }

    public List<Transaction> getTransactions(){
        return this.transactions;
    }

    public ItemTransactions(int item, List<Transaction> transactions) {
        this.item = item;
        this.transactions = transactions;
    }
}
