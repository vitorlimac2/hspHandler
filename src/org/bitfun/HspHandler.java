package org.bitfun;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;


public class HspHandler {

    private static String[] args = null;
    private static Options options = new Options();
    private static final Logger log = Logger.getLogger(HspHandler.class.getName());
    private static String hspFilePath;
    private static float overlap = 5;
    private static float evalue = 0.05f;
    private static boolean isCoverage = false;
    private static boolean isStats = false;
    private static boolean isFilter = false;
    private static boolean isChain = false;
    private static int intronDistance = 10000;


    private HspHandler(String[] args){
        HspHandler.args = args;

        options.addOption("h", "help", false, "Show help.");

        options.addOption("i",true,"Blastn output file sorted by query name and it must be: \n " +
                "* Formatted using the Blastn format option: \n" +
                "\t\t -outfmt \"6 qseqid qlen qstart qend sacc sstart send sstrand evalue bitscore mismatch gaps qseq sseq btop\" \n" +
                "* Sorted by qseqid, sacc, sstart, send and sstrand: \n" +
                "\t\t sort -k1,1 -k5,5 -k6,6n -k7,7n -k8,8 <arg>");

        options.addOption("c",false,"Group full alignments and calculate the coverage by query sequence. " +
                "The file must be filtered by HSP Handler.");

        options.addOption("o", true, "Set overlap threshold (nucleotides) between two HSPs (query-coordinates) " +
                "(Default: 5 nt).");

        options.addOption("d", true, "Maximal distance between two HSPs of the same query and subject " +
                "(Default: 10,000 nt).");

        options.addOption("e", true, "Minimum e-value of HSP. (Default: 0.05)");

        options.addOption("stats", false, "Calculate mismatch and insertion/deletion statistics.");
        options.addOption("filter",false, "Remove redundant HSP for the same query, sequence and strand.");
        options.addOption("chain", false, "Chaining and select the best HSP set for each query sequence.");


        System.err.println("HSP Handler v1.0\nAuthor: Vitor Coelho (vitorlimac2@gmail.com)");

    }

    private void parse() {

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                help();

            if (cmd.hasOption("i")) {
                hspFilePath = cmd.getOptionValue("i");
            } else {
                log.log(Level.SEVERE, "Missing option -i. Provide a path to HSP file.");
                help();
            }

            if(cmd.hasOption("c")){
                isCoverage = true;
            }else if(cmd.hasOption("filter")){
                isFilter = true;
            } else if(cmd.hasOption("stats")){
                isStats = true;
            }
            else if(cmd.hasOption("chain")){
                isChain = true;
            }else{
                log.log(Level.SEVERE, "Missing mode option. Select -filter to reduce" +
                        " the redundant HPS. After filtering, you can calculate the coverage of" +
                        "the read/alignment with the option -c.");
                help();
            }

            if(cmd.hasOption("filter")){
                isFilter = true;
            }

            if(cmd.hasOption("c")){
                isCoverage = true;
            }

            if(cmd.hasOption("o")){
                overlap = Float.parseFloat(cmd.getOptionValue("o"));
            }
            if(cmd.hasOption("e")){
                evalue = Float.parseFloat(cmd.getOptionValue("e"));
            }
            if(cmd.hasOption("d")){
                intronDistance = Integer.parseInt(cmd.getOptionValue("d"));
            }



        } catch (ParseException e) {
            log.log(Level.SEVERE, "Failed to parse command line properties", e);
            help();
        }

        String log_args="";

        assert cmd != null;
        for(Option s : cmd.getOptions()){


            if(s.hasArg()){
                log_args += "-"+s.getOpt() + " " + s.getValue()+" ";
            }else{
                log_args += "-"+s.getOpt() +" ";
            }
        }

        log.log(Level.INFO, "Using argument " + log_args);

    }

    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("HSP Handler", options);
        System.exit(0);
    }

    public static void main(String[] args) {
	// write your code here
        new HspHandler(args).parse();

        // Read file line by line

        try(BufferedReader br = new BufferedReader(new FileReader(hspFilePath))) {

            log.log(Level.INFO, "Starting...");

            if(isFilter) {
                filter(br);
            }else if(isCoverage) {
                reduceRedundanceByRead(br);
            }else if(isChain){
                chainHsp(br);
            }else if(isStats){
                calculateErrorStatistics(br);
            }
            // line is not visible here.
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("Finished.");

    }

    private static void chainHsp(BufferedReader br) throws IOException {

        log.log(Level.INFO, "Chaining filtered HSPs...");

        List<Hsp> hspList = new ArrayList<>();

        for(String line; (line = br.readLine()) != null; ) {
            // process the line.

            String [] splittedHsp = line.split("\t");
            String readID = splittedHsp[0];
            Hsp hsp = parseHsp(splittedHsp);

            if(hspList!=null && hspList.size()!=0){
                if(!hspList.get(0).getQid().equals(readID)){
                    printBestChain(hspList);
                    hspList.clear();
                }
            }
            hspList = selectBestHsp2(hspList, hsp);
        }
        printBestChain(hspList);

        log.log(Level.INFO, "Finished.");
    }

    private static void filter(BufferedReader br) throws IOException {

        log.log(Level.INFO, "Filtering...");

        List<Hsp> hspList = new ArrayList<>();

        for(String line; (line = br.readLine()) != null; ) {
            // process the line.

            String [] splittedHsp = line.split("\t");
            String readID = splittedHsp[0];
            Hsp hsp = parseHsp(splittedHsp);

            if(hspList!=null && hspList.size()!=0){
                if(!hspList.get(0).getQid().equals(readID)){
                    printHsp(hspList);
                    hspList.clear();
                }
            }
            hspList = selectBestHsp2(hspList, hsp);
        }
            printHsp(hspList);

        log.log(Level.INFO, "Finished.");
    }

    private static void printBestChain(List<Hsp> listHsp){
        Collections.sort(listHsp, new HspListComparator());

       Map<String, List<Hsp>> chainMap = new HashMap<>();

       for(Hsp hsp: listHsp){
           String key = hsp.getSid()+hsp.getStrand();
           if(chainMap.containsKey(key)){
               chainMap.get(key).add(hsp);
           }else{
               List<Hsp> l = new ArrayList<>();
               l.add(hsp);
               chainMap.put(key,l);
           }
       }
       String bestHspKey = "";
       float bestScore = 0;

       for(Map.Entry<String, List<Hsp>> element: chainMap.entrySet()){

           float sumScore = 0;

           for(Hsp hsp: element.getValue()){
               sumScore+= hsp.getScore();
           }

           if(sumScore > bestScore){
               bestHspKey = element.getKey();
               bestScore = sumScore;
           }
       }
       printHsp(chainMap.get(bestHspKey));

    }

    private static void reduceRedundanceByRead(BufferedReader br) throws IOException {
        HspChain bestFilteredHsp = null;

        for(String line; (line = br.readLine()) != null; ) {
            // process the line.

            String [] splittedHsp = line.split("\t");

            // Read Length Sequence Strand Score Distance Mismatch Gaps
            // 7410406	264	chr9	+	251.0	263	42	9

            float coverage = Float.parseFloat(splittedHsp[5]) > Float.parseFloat(splittedHsp[1])?100:
                    Float.parseFloat(splittedHsp[5])*100/Float.parseFloat(splittedHsp[1]);


            HspChain hspChain = new HspChain();

            hspChain.setQid(splittedHsp[0]);
            hspChain.setLength(Integer.parseInt(splittedHsp[1]));
            hspChain.setSid(splittedHsp[2]);
            hspChain.setStrand(splittedHsp[3].charAt(0));
            hspChain.setScore(Float.parseFloat(splittedHsp[4]));
            hspChain.setCoverage(coverage);
            hspChain.setMismatch(Integer.parseInt(splittedHsp[6]));
            hspChain.setGaps(Integer.parseInt(splittedHsp[7]));
            hspChain.setQregions(null);
            hspChain.setSregions(null);

            if(bestFilteredHsp==null) {
                bestFilteredHsp = hspChain;
                continue;
            }

            if(!bestFilteredHsp.getQid().equals(hspChain.getQid())){
                System.out.println(bestFilteredHsp.toString());
                bestFilteredHsp = hspChain;
            }else {
                if(bestFilteredHsp.getScore() < hspChain.getScore()) {
                    bestFilteredHsp = hspChain;
                }
            }
        }

        if(bestFilteredHsp!=null){
            System.out.println(bestFilteredHsp.toString());
        }
    }


    private static void printHsp(List<Hsp> hspList) {

        if(hspList!= null && hspList.size()!=0){

            Collections.sort(hspList, new HspListComparator());

            for(Hsp hsp : hspList)
                System.out.println(hsp.toString());
        }
    }

    private static List<Hsp> selectBestHsp(List<Hsp> l, Hsp newHit){


        if(newHit.getEvalue()> evalue)
            return l;

        if (l==null)
            l = new ArrayList<>();

        Collections.sort(l, new HspListComparator());

        //HspScoreComparator hc = new HspScoreComparator();
        //l.sort(hc); // sort by score. If equal scores, sort by evalue

        List<Hsp> hitAuxList = new ArrayList<>();

        for(org.bitfun.Hsp h: l) {
            // has overlapping

            // has overlapping
            if(newHit.getQstart() <= h.getQend() &&
                    newHit.getQend() >= h.getQstart()) {

                int posA = h.getQstart();
                int posB = h.getQend();

                int posX = newHit.getQstart();
                int posY = newHit.getQend();

                int diff;

                if (posX <= posA && posA <= posY) {
                    diff = posY - posA + 1;
                } else if (posX <= posB && posB <= posY) {
                    diff = posB - posX + 1;
                } else {
                    diff = posB - posA + 1;
                }

                // if hit overlapping > threshold AND same read AND same strand
                // AND new hit score < older hit score are the conditions to reject a new hit
                if (diff > overlap && h.getSid().equals(newHit.getSid()) &&
                        h.getStrand() == newHit.getStrand()) {
                    // has overlapping
                    if (h.getScore() > newHit.getScore() ||
                            (h.getScore() == newHit.getScore() && h.getEvalue() <= newHit.getEvalue())) {
                        return l;
                    } else {
                        hitAuxList.add(h);
                    }
                }
            }



        }


        l.removeAll(hitAuxList);
        l.add(newHit);
        return l;

    }

    private static List<Hsp> selectBestHsp2(List<Hsp> l, Hsp newHit){


        if(newHit.getEvalue()> evalue)
            return l;

        if (l==null)
            l = new ArrayList<>();

        Collections.sort(l, new HspListComparator());

        //HspScoreComparator hc = new HspScoreComparator();
        //l.sort(hc); // sort by score. If equal scores, sort by evalue

        List<Hsp> hitAuxList = new ArrayList<>();

        List<Hsp> hitAuxList2 = new ArrayList<>();

        int lastSend = 0;

        float sumScore = 0;

        boolean sameSubject = false;

        for(Hsp h: l) {

            if(h.getSid().equals(newHit.getSid()) &&
                    h.getStrand()==newHit.getStrand()){

                hitAuxList2.add(h);

                //has overlapping

                sumScore += h.getScore();
                sameSubject = true;
                lastSend = h.getSend();

                if(newHit.getQstart() <= h.getQend() &&
                        newHit.getQend() >= h.getQstart()){

                    int posA = h.getQstart();
                    int posB = h.getQend();

                    int posX = newHit.getQstart();
                    int posY = newHit.getQend();

                    int diff;

                    if (posX <= posA && posA <= posY) {
                        diff = posY - posA + 1;
                    } else if (posX <= posB && posB <= posY) {
                        diff = posB - posX + 1;
                    } else {
                        diff = posB - posA + 1;
                    }

                    // if hit overlapping > threshold AND same read AND same strand
                    // AND new hit score < older hit score are the conditions to reject a new hit
                    if (diff > overlap) {
                        // has overlapping
                        if (h.getScore() > newHit.getScore() ||
                                (h.getScore() == newHit.getScore() && h.getEvalue() <= newHit.getEvalue())) {
                            return l;
                        } else {
                            hitAuxList.add(h); // remove h
                        }
                    }

                }
            }
        }

        if(hitAuxList.size() == 0 && sameSubject){ // has no overlap
            if(newHit.getSstart() - lastSend + 1 > intronDistance){ // too much distant
                if(newHit.getScore() > sumScore){
                    l.removeAll(hitAuxList2);
                    l.add(newHit);
                }
                return l;
            }
        }

        l.removeAll(hitAuxList);
        l.add(newHit);
        return l;

    }

    /**
     * HspScoreComparator compares its two hsp for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     */
    public static class HspScoreComparator implements Comparator<Hsp> {

        /**
         * Compare two hits about bit score.
         * @param o1 Hit object
         * @param o2 Hit object
         * @return a negative integer, zero, or a positive integer as the first argument
         * is less than, equal to, or greater than the second, respectively.
         */
        @Override
        public int compare(Hsp o1, Hsp o2) {
            if(o1.getScore() < o2.getScore()){
                return -1;
            }else if(o1.getScore() > o2.getScore()){
                return 1;
            }else if(o1.getEvalue() < o2.getEvalue()){
                return 1;
            }else if(o1.getEvalue() > o2.getEvalue()){
                return -1;
            }else
                return 0;
        }
    }

/* ############################################################################################
###############################################################################################
 */

    /** Parse Blastn tabular output. Output format: -outfmt "6 qseqid qlen qstart qend sacc sstart send sstrand evalue bitscore mismatch gaps qseq sseq btop"
     * @param splittedHsp String array with the information of each HSPs of the Blastn tabular output.
     * @return Return an HSP object.
     */

    private static Hsp parseHsp(String [] splittedHsp){
        Hsp hsp = new Hsp();
        hsp.setQid(splittedHsp[0]);
        hsp.setLength(Integer.parseInt(splittedHsp[1]));
        hsp.setQstart(Integer.parseInt(splittedHsp[2]));
        hsp.setQend(Integer.parseInt(splittedHsp[3]));
        hsp.setSid(splittedHsp[4]);
        hsp.setSstart(Integer.parseInt(splittedHsp[5]));
        hsp.setSend(Integer.parseInt(splittedHsp[6]));
        hsp.setSstrand(splittedHsp[7].equals("plus")?'+':'-');
        hsp.setEvalue(Float.parseFloat(splittedHsp[8]));
        hsp.setScore(Float.parseFloat(splittedHsp[9]));
        hsp.setMismatch(Integer.parseInt(splittedHsp[10]));
        hsp.setGaps(Integer.parseInt(splittedHsp[11]));
        hsp.setQseq(splittedHsp[12]);
        hsp.setSseq(splittedHsp[13]);
        hsp.setBtop(splittedHsp[14]);
        return hsp;
    }


    /**
     * Calculate mismatches/indels for a BLAST input on format : -outfmt "6 qseqid qlen sacc qstart qend qseq sstart send evalue bitscore pident mismatch gaps sseq btop"
     * @param br BufferedRead object used to read each line of BLAST output.
     * @throws IOException It is not possible to read the input file line.
     */

    private static void calculateErrorStatistics(BufferedReader br) throws IOException {

        log.log(Level.INFO, "Finding mismatch/indels...");

        for(String line; (line = br.readLine()) != null; ) {
            // process the line.
            String [] splittedHsp = line.split("\t");
            Hsp hsp = parseHsp(splittedHsp);
            printMismatchPositions(hsp);
            printDelPositions(hsp);
            printInsertionPositions(hsp);
        }

        log.log(Level.INFO, "Finished.");
    }

    private static void printMismatchPositions(Hsp h){

        boolean isMismatch = false;

        int totalCountDeletions = 0;
        int totalCountInsertions = 0;

        String qseq = h.getQseq();
        String sseq = h.getSseq();

        for (int i = 0; i < h.getQseq().length();i++) {

            if(qseq.charAt(i)!=sseq.charAt(i) && qseq.charAt(i)!='-' && sseq.charAt(i)!='-'){

                if(i == h.getQseq().length()-1){
                    if(qseq.charAt(i-1)!='-' && sseq.charAt(i-1)!='-'
                        && qseq.charAt(i-1)== sseq.charAt(i-1)){ // Allow only SNV

                        isMismatch = true;
                    }
                }else if(i==0) {
                    if(qseq.charAt(i+1)!='-' && sseq.charAt(i+1)!='-'
                        && qseq.charAt(i+1)== sseq.charAt(i+1) // Allow only SNV
                            ){
                        isMismatch = true;
                    }
                }else if(qseq.charAt(i-1)!='-' && sseq.charAt(i-1)!='-' &&
                        qseq.charAt(i+1)!='-' && sseq.charAt(i+1)!='-'
                    && qseq.charAt(i-1)== sseq.charAt(i-1) && //Allow only SNV
                    qseq.charAt(i+1)== sseq.charAt(i+1) // Allow only SNV
                        ){
                    isMismatch = true;
                }
            }

            if(qseq.charAt(i)=='-'){
                totalCountDeletions++;
            }

            if(sseq.charAt(i)=='-'){
                totalCountInsertions++;
            }

            if(isMismatch) {
                printError(h.getQid(),
                        h.getLength(),
                        h.getQstart(),
                        h.getSstart(),
                        (h.getQstart() + i - totalCountDeletions),
                        (h.getSstart() + i - totalCountInsertions),
                        Character.toString(qseq.charAt(i)),
                        Character.toString(sseq.charAt(i)),
                        h.getBtop(),
                        h.getQseq(),
                        h.getSseq()
                );
                isMismatch = false;
            }
        }
    }

    private static void printDelPositions(Hsp h){

        int lenght_del = 0;

        int start_del = 0;
        int start_del_seq = 0;
        String deletion_seq = "";

        boolean endDeletionExtension = true;

        int totalCountDeletions = 0;
        int totalCountInsertions = 0;

        String qseq = h.getQseq();
        String sseq = h.getSseq();

        if(!qseq.contains("-")){
            return;
        }

        for (int i = 0; i < h.getQseq().length();i++) {



            if(sseq.charAt(i)=='-'){
                totalCountInsertions++;
            }

            //deletion
            if(qseq.charAt(i)=='-') {

                endDeletionExtension = false;

                if (lenght_del == 0) {
                    start_del = i - totalCountDeletions;
                    start_del_seq = i - totalCountInsertions;
                }

                totalCountDeletions++;

                lenght_del++;
                deletion_seq += sseq.charAt(i);

            }

            if(qseq.charAt(i)!='-' || i == h.getQseq().length()-1){
                endDeletionExtension = true;
            }

            if(lenght_del>0 && endDeletionExtension){

                printError(h.getQid(), h.getLength(),
                        h.getQstart(),
                        h.getSstart(),
                        (h.getQstart() + start_del - 1),
                        (h.getSstart() + start_del_seq),
                        "*" ,
                        deletion_seq,
                        h.getBtop(),
                        qseq ,
                        sseq);

                lenght_del=0;
                deletion_seq="";
                endDeletionExtension = false;
            }
        }
    }

    private static void printInsertionPositions(Hsp h){

        int length_ins = 0;

        int start_ins = 0;
        int start_ins_seq = 0;
        String insertion_seq = "";

        boolean endInsertionExtension = true;

        int totalCountDeletions = 0;
        int totalCountInsertions = 0;

        String qseq = h.getQseq();
        String sseq = h.getSseq();


        if(!sseq.contains("-"))
            return;

        for (int i = 0; i < h.getQseq().length();i++) {


            if(qseq.charAt(i)=='-'){
                totalCountDeletions++;
            }

            //insertion

            if(sseq.charAt(i)=='-') {
                endInsertionExtension = false;
                if (length_ins == 0) {
                    start_ins = i - totalCountInsertions;
                    start_ins_seq = i - totalCountDeletions;
                }

                totalCountInsertions++;
                length_ins++;
                insertion_seq += qseq.charAt(i);
            }

            if(sseq.charAt(i)!='-' || i == h.getQseq().length()-1){
                endInsertionExtension = true;
            }

            if(length_ins>0 && endInsertionExtension){

                printError(h.getQid(), h.getLength(),h.getQstart(), h.getSstart(),
                        (h.getQstart() + start_ins), (h.getSstart() + start_ins_seq - 1),
                        insertion_seq,"*",h.getBtop(),qseq,sseq);
                length_ins = 0;
                endInsertionExtension = true;
                insertion_seq = "";
            }
        }
    }

    private static void printError(String qId, int length,
                                   int qStart, int sStart,
                                   int qStartError, int sStartError,
                                   String ref, String error,
                                   String btop, String qSeq,
                                   String sSeq){

        System.out.println(qId + "\t" +
                length + "\t" +
                qStart + "\t" +
                sStart + "\t" +
                qStartError + "\t" +
                sStartError + "\t" +
                ref + "\t" +
                error + "\t" +
                btop + "\t" +
                qSeq + "\t" +
                sSeq);

    }

}

