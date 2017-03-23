package org.bitfun;


/**
 * Created by vitor on 15/03/17.
 */
public class HspFormatterTest {

    public static void main(String[ ] args){

        obtainIndelPositions();

    }
    public static void obtainIndelPositions(){

        Hsp h = new Hsp();

        int length_del = 0;
        int length_ins = 0;

        int start_del = 0;
        String deletion_seq = "";

        int start_ins = 0;
        String insertion_seq = "";

        int h_qstart = 10;
        int h_sstart = 1000;

        boolean endDeletionExtension = true;
        boolean endInsertionExtension = true;

        for (int i = 0; i < 12;i++) {

            String qseq = "--ACTTCC-G-C";
            String sseq = "AAATT--CCGGC";

            //deletion
            if(qseq.charAt(i)=='-') {

                endDeletionExtension = false;

                if (length_del == 0) {
                    start_del = i;
                }

                length_del++;
                deletion_seq += sseq.charAt(i);

            }

            if(qseq.charAt(i)!='-' || i == 11){
                endDeletionExtension = true;
            }

            if(length_del>0 && endDeletionExtension){

                System.out.println(h.getQid() + "\t" + h.getLength() + "\t" + 12 + "\t" +
                        (h_qstart + start_del + 1) + "\t" +
                        (h_sstart + start_del + 1) + "\t" +
                        length_del + "\t" +
                        deletion_seq + "\t" +
                        "*");

                length_del=0;
                deletion_seq="";

            }

            /////////////////////////////////////////////////////////////////////////
            //
            // insertion

            if(sseq.charAt(i)=='-') {

                endInsertionExtension = false;

                if (length_del == 0) {
                    start_del = i;
                }

                length_del++;
                deletion_seq += sseq.charAt(i);

            }

            if(qseq.charAt(i)!='-' || i == 11){
                endDeletionExtension = true;
            }

            if(length_del>0 && endDeletionExtension){

                System.out.println(h.getQid() + "\t" + h.getLength() + "\t" + 12 + "\t" +
                        (h_qstart + start_del + 1) + "\t" +
                        (h_sstart + start_del + 1) + "\t" +
                        length_del + "\t" +
                        deletion_seq + "\t" +
                        "*");

                length_del=0;
                deletion_seq="";

            }
        }

    }
}
