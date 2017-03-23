package org.bitfun;

/**
 * Created by vitor on 14/02/17.
 */
public class Hsp {


    /**
     * Query sequence identifier.
     */
    private String qid;

    /**
     * Length of query
     */
    private int length;

    /**
     * Sequence identifier.
     */
    private String sid;

    /**
     * Query Alignment Start.
     */

    private int qstart;

    /**
     * Query Alignment End.
     */

    private int qend;

    /**
     * Alignment Strand.
     */
    private char sstrand;


    /**
     * Sequence Alignment Start.
     */

    private int sstart;

    /**
     * Sequence Alignment End.
     */

    private int send;

    /**
     * Score.
     */

    private float score;

    /**
     * E-value.
     */

    private float evalue;

    /**
     * Mismatches.
     */

    private int mismatch;

    /**
     * Percentage of identity.
     */

    private float pident;

    /**
     * Number of gaps.
     */

    private int gaps;

    /**
     * Aligned part of query sequence
     */
    private String qseq;
    /**
     * Aligned part of subject sequence
     */
    private String sseq;

    /**
     * Blast traceback operations
     */
    private String btop;

    public void setQid(String qid) {
        this.qid = qid;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setQstart(int qstart) {
        this.qstart = qstart;
    }

    public void setQend(int qend) {
        this.qend = qend;
    }

    public void setSstrand(char strand) {
        this.sstrand = strand;
    }

    public void setSstart(int sstart) {
        this.sstart = sstart;
    }

    public void setSend(int send) {
        this.send = send;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setEvalue(float evalue) {
        this.evalue = evalue;
    }

    public void setMismatch(int mismatch) {
        this.mismatch = mismatch;
    }

    public void setPident(float pident) {
        this.pident = pident;
    }

    public void setGaps(int gaps) {
        this.gaps = gaps;
    }

    public void setQseq(String qseq) {
        this.qseq = qseq;
    }

    public void setSseq(String sseq) {
        this.sseq = sseq;
    }

    public void setBtop(String btop) {
        this.btop = btop;
    }

    public String getBtop(){
        return this.btop;
    }

    public String getQid() {
        return qid;
    }

    public String getSid() {
        return sid;
    }

    int getQstart() {
        return qstart;
    }

    int getQend() {
        return qend;
    }


    float getScore() {
        return score;
    }

    float getEvalue() {
        return evalue;
    }

    int getMismatch() {
        return mismatch;
    }


    public int getGaps() {
        return gaps;
    }

    public String getQseq() {
        return qseq;
    }

    public String getSseq() {
        return sseq;
    }

    public int getLength() {
        return length;
    }

    public char getStrand() {
        return sstrand;
    }

    public int getSstart() {
        return sstart;
    }

    public int getSend() {
        return send;
    }

    public float getPident() {
        return pident;
    }

    /**
     * Output HSP
     * -outfmt "6 qseqid qlen qstart qend sacc sstart send sstrand evalue bitscore mismatch gaps qseq sseq btop"
     * @return
     */
    @Override
    public String toString() {
        return qid + '\t'
                + length + '\t'
                + qstart + '\t'
                + qend + '\t'
                + sid + '\t'
                + sstart + '\t'
                + send + '\t'
                + sstrand + '\t'
                + evalue + '\t'
                + score + '\t'
                + mismatch + '\t'
                + gaps + '\t'
                + qseq + '\t'
                + sseq + '\t'
                + btop;
    }


}