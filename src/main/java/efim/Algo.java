package efim;

import java.io.Serializable;
import java.util.List;

/**
 * Created by juanfranfv on 8/2/17.
 */
public class Algo implements Serializable {
    public List<Transaction> transactions;
    public int item;
    public List<Integer> itemsToKeep;
    public List<Integer> itemsToExplore;


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

    public void setItemsToKeep(List<Integer> items){
        this.itemsToKeep = items;
    }

    public List<Integer> getItemsToKeep(){
        return this.itemsToKeep;
    }

    public void setItemsToExplore(List<Integer> items){
        this.itemsToExplore = items;
    }

    public List<Integer> getItemsToExplore(){
        return this.itemsToExplore;
    }

    public Algo(int i, List<Transaction> ts, List<Integer> itemsK, List<Integer> itemsE) {
        this.item = i;
        this.transactions = ts;
        this.itemsToExplore = itemsE;
        this.itemsToKeep = itemsK;
    }
}
