package org.bitfun;

import java.util.Comparator;

/**
 * Created by vitor on 20/03/17.
 */
public class HspListComparator implements Comparator{
    @Override
    public int compare(Object o1, Object o2) {

        String qid1 = ((Hsp) o1).getQid();
        String qid2 = ((Hsp) o2).getQid();

        String sid1 = ((Hsp) o1).getSid();
        String sid2 = ((Hsp) o1).getSid();

        char sstrand1 = ((Hsp) o1).getStrand();
        char sstrand2 = ((Hsp) o2).getStrand();

        int qstart1 = ((Hsp) o1).getSstart();
        int qend1 = ((Hsp) o1).getQend();

        int qstart2 = ((Hsp) o2).getSstart();
        int qend2 = ((Hsp) o2).getQend();

        int qidCompare = qid1.compareTo(qid2);
        int sidCompare = sid1.compareTo(sid2);

        int sstrandCompare;

        if(sstrand1 < sstrand2){
            sstrandCompare = 1;
        }else if(sstrand1 > sstrand2){
            sstrandCompare = -1;
        } else{
            sstrandCompare = 0;
        }

        int qstartCompare;
        if(qstart1 < qstart2){
            qstartCompare = -1;
        }else if(qstart1 > qstart2){
            qstartCompare = 1;
        } else{
            qstartCompare = 0;
        }

        int qendCompare;
        if(qend1 < qend2){
            qendCompare = -1;
        }else if(qend1 > qend2){
            qendCompare = 1;
        } else{
            qendCompare = 0;
        }

        if(qidCompare!=0){
            return qidCompare;
        }else if(sidCompare!=0){
            return sidCompare;
        }else if(sstrandCompare!=0){
            return sstrandCompare;
        }else if(qstartCompare!=0){
            return qstartCompare;
        }else{
            return qendCompare;
        }
    }
}
