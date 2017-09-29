package efim;

import org.apache.spark.util.CollectionAccumulator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by juanfranfv on 8/29/17.
 */
final public class EFIMiner implements Serializable {
    List<Output> results;
    int candidateCount;
    long minUtil;
    private boolean activateTransactionMerging;
    private boolean activateSubtreeUtilityPruning;
    List<Transaction> transactions;
    int transactionReadingCount;
    int mergeCount;
    final int MAXIMUM_SIZE_MERGING = 1000;

    /** a temporary buffer */
    private int [] temp= new int [500];

    /** The following variables are the utility-bins array
     // Recall that each bucket correspond to an item */
    /** utility bin array for sub-tree utility */
    private long[] utilityBinArraySU;
    /** utility bin array for local utility */
    private long[] utilityBinArrayLU;

    int[] newNamesToOldNames;

    List<Integer> elementos;

    public EFIMiner(long minUtil, List<Transaction> transactions, boolean activateTransactionMerging,
                    int[] newNamesToOldNames, int newItemCount) {
        this.candidateCount = 0;
        this.transactionReadingCount = 0;
        this.mergeCount = 0;
        this.results = new ArrayList<Output>();
        this.activateTransactionMerging = activateTransactionMerging;
        this.activateSubtreeUtilityPruning = activateTransactionMerging;
        this.minUtil = minUtil;
        this.transactions = transactions;
        this.newNamesToOldNames = newNamesToOldNames;
        this.utilityBinArraySU = new long[newItemCount + 1];
        this.utilityBinArrayLU = new long[newItemCount + 1];
    }

    public List<Output> Mine(Integer e, List<Integer> itemsToKeep, List<Integer> itemsToExplore) throws IOException {
        //candidateCount++;
        elementos = new ArrayList<>();
        Miner(e, itemsToKeep, itemsToExplore, transactions, 0);

        return results;
    }

    public void Miner(Integer e, List<Integer> itemsToKeep, List<Integer> itemsToExplore, List<Transaction> transactionsOfP, int prefixLength) throws IOException {
        int j= itemsToExplore.indexOf(e);
//        if(e == 44 & prefixLength == 0){
//            System.out.println("Hola");
//        }
        elementos.add(e);
//        if(e == 2){
//            System.out.println(e);
//        }
        // ========== PERFORM INTERSECTION =====================
        // Calculate transactions containing P U {e}
        // At the same time project transactions to keep what appears after "e"
        List<Transaction> transactionsPe = new ArrayList<Transaction>();

        // variable to calculate the utility of P U {e}
        int utilityPe = 0;

        // For merging transactions, we will keep track of the last transaction read
        // and the number of identical consecutive transactions
        Transaction previousTransaction = null;
        int consecutiveMergeCount = 0;

        // this variable is to record the time for performing intersection
        long timeFirstIntersection = System.currentTimeMillis();

        // For each transaction
        for(Transaction transaction : transactionsOfP) {
            // Increase the number of transaction read
            transactionReadingCount++;

            // To record the time for performing binary searh
            long timeBinaryLocal = System.currentTimeMillis();

            // we remember the position where e appears.
            // we will call this position an "offset"
            int positionE = -1;
            // Variables low and high for binary search
            int low = transaction.offset;
            int high = transaction.items.length - 1;

            // perform binary search to find e in the transaction
            while (high >= low ) {
                int middle = (low + high) >>> 1; // divide by 2
                if (transaction.items[middle] < e) {
                    low = middle + 1;
                }else if (transaction.items[middle] == e) {
                    positionE =  middle;
                    break;
                }  else{
                    high = middle - 1;
                }
            }
            // record the time spent for performing the binary search
//            timeBinarySearch +=  System.currentTimeMillis() - timeBinaryLocal;

//	        	if(prefixLength == 0 && newNamesToOldNames[e] == 385) {
//		        	for(int i=0; i < transaction.getItems().length; i++) {
//		        		if(transaction.getItems()[i] == e) {
//		        			innerSum += transaction.getUtilities()[i];
//		        		}
//		        	}
//		        }

            // if 'e' was found in the transaction
            if (positionE > -1  ) {

                // optimization: if the 'e' is the last one in this transaction,
                // we don't keep the transaction
                if(transaction.getLastPosition() == positionE){
                    // but we still update the sum of the utility of P U {e}
                    utilityPe  += transaction.utilities[positionE] + transaction.prefixUtility;
                }else{
                    // otherwise
                    if(activateTransactionMerging && MAXIMUM_SIZE_MERGING >= (transaction.items.length - positionE)){
                        // we cut the transaction starting from position 'e'
                        Transaction projectedTransaction = new Transaction(transaction, positionE);
                        utilityPe  += projectedTransaction.prefixUtility;

                        // if it is the first transaction that we read
                        if(previousTransaction == null){
                            // we keep the transaction in memory
                            previousTransaction = projectedTransaction;
                        }else if (isEqualTo(projectedTransaction, previousTransaction)){
                            // If it is not the first transaction of the database and
                            // if the transaction is equal to the previously read transaction,
                            // we will merge the transaction with the previous one

                            // increase the number of consecutive transactions merged
                            mergeCount++;

                            // if the first consecutive merge
                            if(consecutiveMergeCount == 0){
                                // copy items and their profit from the previous transaction
                                int itemsCount = previousTransaction.items.length - previousTransaction.offset;
                                int[] items = new int[itemsCount];
                                System.arraycopy(previousTransaction.items, previousTransaction.offset, items, 0, itemsCount);
                                long[] utilities = new long[itemsCount];
                                System.arraycopy(previousTransaction.utilities, previousTransaction.offset, utilities, 0, itemsCount);

                                // make the sum of utilities from the previous transaction
                                int positionPrevious = 0;
                                int positionProjection = projectedTransaction.offset;
                                while(positionPrevious < itemsCount){
                                    utilities[positionPrevious] += projectedTransaction.utilities[positionProjection];
                                    positionPrevious++;
                                    positionProjection++;
                                }

                                // make the sum of prefix utilities
                                long sumUtilities = previousTransaction.prefixUtility += projectedTransaction.prefixUtility;

                                // create the new transaction replacing the two merged transactions
                                previousTransaction = new Transaction(items, utilities, previousTransaction.transactionUtility + projectedTransaction.transactionUtility);
                                previousTransaction.prefixUtility = sumUtilities;

                            }else{
                                // if not the first consecutive merge

                                // add the utilities in the projected transaction to the previously
                                // merged transaction
                                int positionPrevious = 0;
                                int positionProjected = projectedTransaction.offset;
                                int itemsCount = previousTransaction.items.length;
                                while(positionPrevious < itemsCount){
                                    previousTransaction.utilities[positionPrevious] += projectedTransaction.utilities[positionProjected];
                                    positionPrevious++;
                                    positionProjected++;
                                }

                                // make also the sum of transaction utility and prefix utility
                                previousTransaction.transactionUtility += projectedTransaction.transactionUtility;
                                previousTransaction.prefixUtility += projectedTransaction.prefixUtility;
                            }
                            // increment the number of consecutive transaction merged
                            consecutiveMergeCount++;
                        }else{
                            // if the transaction is not equal to the preceding transaction
                            // we cannot merge it so we just add it to the database
                            transactionsPe.add(previousTransaction);
                            // the transaction becomes the previous transaction
                            previousTransaction = projectedTransaction;
                            // and we reset the number of consecutive transactions merged
                            consecutiveMergeCount = 0;
                        }
                    }else{
                        // Otherwise, if merging has been deactivated
                        // then we just create the projected transaction
                        Transaction projectedTransaction = new Transaction(transaction, positionE);
                        // we add the utility of Pe in that transaction to the total utility of Pe
                        utilityPe  += projectedTransaction.prefixUtility;
                        // we put the projected transaction in the projected database of Pe
                        transactionsPe.add(projectedTransaction);
                    }
                }
                // This is an optimization for binary search:
                // we remember the position of E so that for the next item, we will not search
                // before "e" in the transaction since items are visited in lexicographical order
                //transaction.offset = positionE;
            }else{
                // This is an optimization for binary search:
                // we remember the position of E so that for the next item, we will not search
                // before "e" in the transaction since items are visited in lexicographical order
                //transaction.offset = low;
            }
        }
        // remember the total time for peforming the database projection
//        timeIntersections += (System.currentTimeMillis() - timeFirstIntersection);

        // Add the last read transaction to the database if there is one
        if(previousTransaction != null){
            transactionsPe.add(previousTransaction);
        }

        // Append item "e" to P to obtain P U {e}
        // but at the same time translate from new name of "e"  to its old name
        temp[prefixLength] = newNamesToOldNames[e];
//			if(DEBUG){
//				System.out.println("Temp: " + temp[prefixLength]);
//			}

        // if the utility of PU{e} is enough to be a high utility itemset
        if(utilityPe  >= minUtil)
        {
//                if(DEBUG){
//                    System.out.println("j: " + j + ". e: " + e);
//					System.out.print("Prefix: " +  prefixLength);
//					System.out.println(". Utility: " +  utilityPe);
//                }

            // output PU{e}
//            output(prefixLength, utilityPe );
            int[] copy = new int[prefixLength+1];
            //temp[prefixLength] = newNamesToOldNames[e];
            System.arraycopy(temp, 0, copy, 0, prefixLength+1);
            results.add(new Output(prefixLength, utilityPe, e, copy));
//            System.out.println(elementos);
        }

//            if(DEBUG){
//                System.out.println("1ro");
//                System.out.println("item: " + e);
//                System.out.println("Transactions: " + transactionsPe);
//            }


        //==== Next, we will calculate the Local Utility and Sub-tree utility of
        // all items that could be appended to PU{e} ====
        useUtilityBinArraysToCalculateUpperBounds(transactionsPe, j, itemsToKeep);
//        if(e == 2){
//            System.out.println("Size: " + transactionsPe.size());
//            System.out.println(Arrays.toString(utilityBinArraySU));
//            //System.out.println(Arrays.toString(utilityBinArrayLU));
//        }
//			if(DEBUG){
//                System.out.println();
//                System.out.println("===== Projected database e: " + e + " === ");
//                for(Transaction tra : transactionsPe){
//                    System.out.println(tra);
//                }
//
//            }
        // we now record time for identifying promising items
        long initialTime = System.currentTimeMillis();

        // We will create the new list of secondary items
        List<Integer> newItemsToKeep = new ArrayList<Integer>();
        // We will create the new list of primary items
        List<Integer> newItemsToExplore = new ArrayList<Integer>();

        // for each item
        for (int k = j+1; k < itemsToKeep.size(); k++) {
            Integer itemk =  itemsToKeep.get(k);

            // if the sub-tree utility is no less than min util
            if(utilityBinArraySU[itemk] >= minUtil) {
                // and if sub-tree utility pruning is activated
                if(activateSubtreeUtilityPruning){
                    // consider that item as a primary item
                    newItemsToExplore.add(itemk);
                }
                // consider that item as a secondary item
                newItemsToKeep.add(itemk);
            }else if(utilityBinArrayLU[itemk] >= minUtil)
            {
                // otherwise, if local utility is no less than minutil,
                // consider this itemt to be a secondary item
                newItemsToKeep.add(itemk);
            }
        }
        // update the total time  for identifying promising items
//        timeIdentifyPromisingItems +=  (System.currentTimeMillis() -  initialTime);
//			System.out.println("NItemsE: " + newItemsToExplore);
        // === recursive call to explore larger itemsets
        if(activateSubtreeUtilityPruning){
            // if sub-tree utility pruning is activated, we consider primary and secondary items
            backtrackingEFIM(transactionsPe, newItemsToKeep, newItemsToExplore,prefixLength+1);
        }else{
            // if sub-tree utility pruning is deactivated, we consider secondary items also
            // as primary items
            backtrackingEFIM(transactionsPe, newItemsToKeep, newItemsToKeep,prefixLength+1);
        }
        //return;
    }

    private void backtrackingEFIM( List<Transaction> transactionsOfP,
                                   List<Integer> itemsToKeep, List<Integer> itemsToExplore, int prefixLength) throws IOException {

        // update the number of candidates explored so far
        candidateCount += itemsToExplore.size();
        //1System.out.println("Entre");
        // ========  for each frequent item  e  =============
        for (int j = 0; j < itemsToExplore.size(); j++) {
            Integer e = itemsToExplore.get(j);
            Miner(e, itemsToKeep, itemsToExplore, transactionsOfP, prefixLength);
        }
    }

    private boolean isEqualTo(Transaction t1, Transaction t2) {
        // we first compare the transaction lenghts
        int length1 = t1.items.length - t1.offset;
        int length2 = t2.items.length - t2.offset;
        // if not same length, then transactions are not identical
        if(length1 != length2){
            return false;
        }
        // if same length, we need to compare each element position by position,
        // to see if they are the same
        int position1 = t1.offset;
        int position2 = t2.offset;

        // for each position in the first transaction
        while(position1 < t1.items.length){
            // if different from corresponding position in transaction 2
            // return false because they are not identical
            if(t1.items[position1]  != t2.items[position2]){
                return false;
            }
            // if the same, then move to next position
            position1++;
            position2++;
        }
        // if all items are identical, then return to true
        return true;
    }

    private void useUtilityBinArraysToCalculateUpperBounds(List<Transaction> transactionsPe,
                                                           int j, List<Integer> itemsToKeep) {

        // we will record the time used by this method for statistics purpose
        long initialTime = System.currentTimeMillis();

        // For each promising item > e according to the total order
        for (int i = j + 1; i < itemsToKeep.size(); i++) {
            Integer item = itemsToKeep.get(i);
            // We reset the utility bins of that item for computing the sub-tree utility and
            // local utility
            utilityBinArraySU[item] = 0;
            utilityBinArrayLU[item] = 0;
        }

        int sumRemainingUtility;
        // for each transaction
        for (Transaction transaction : transactionsPe) {
            // count the number of transactions read
            transactionReadingCount++;

            // We reset the sum of reamining utility to 0;
            sumRemainingUtility = 0;
            // we set high to the last promising item for doing the binary search
            int high = itemsToKeep.size() - 1;

            // for each item in the transaction that is greater than i when reading the transaction backward
            // Note: >= is correct here. It should not be >.
            for (int i = transaction.getItems().length - 1; i >= transaction.offset; i--) {
                // get the item
                int item = transaction.getItems()[i];

                // We will check if this item is promising using a binary search over promising items.

                // This variable will be used as a flag to indicate that we found the item or not using the binary search
                boolean contains = false;
                // we set "low" for the binary search to the first promising item position
                int low = 0;

                // do the binary search
                while (high >= low) {
                    int middle = (low + high) >>> 1; // divide by 2
                    int itemMiddle = itemsToKeep.get(middle);
                    if (itemMiddle == item) {
                        // if we found the item, then we stop
                        contains = true;
                        break;
                    } else if (itemMiddle < item) {
                        low = middle + 1;
                    } else {
                        high = middle - 1;
                    }
                }
                // if the item is promising
                if (contains) {
                    // We add the utility of this item to the sum of remaining utility
                    sumRemainingUtility += transaction.getUtilities()[i];
                    // We update the sub-tree utility of that item in its utility-bin
                    utilityBinArraySU[item] += sumRemainingUtility + transaction.prefixUtility;
                    // We update the local utility of that item in its utility-bin
                    utilityBinArrayLU[item] += transaction.transactionUtility + transaction.prefixUtility;
                }
            }
        }
        // we update the time for database reduction for statistics purpose
//        timeDatabaseReduction += (System.currentTimeMillis() - initialTime);
    }
}
