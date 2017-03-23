package org.bitfun;

import java.util.List;

/**
 * Created by vitor on 16/02/17.
 */
public class HspChain {

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
     * Alignment Strand.
     */
    private char strand;


    /**
     * Total Score.
     */

    private float score;

    /**
     * Total Mismatches.
     */

    private int mismatch;

    /**
     * Covarage.
     */

    private float coverage;

    /**
     * Total gaps.
     */

    private int gaps;

    /**
     * Query Alignment Distances
     */

    private List<Integer> qDistance;

    /**
     * Genomic Alignment Distances
     */

    private List<Integer> gDistances;

    String getQid() {
        return qid;
    }

    float getScore() {
        return score;
    }

    public void setQid(String qid) {
        this.qid = qid;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public char getStrand() {
        return strand;
    }

    public void setStrand(char strand) {
        this.strand = strand;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getMismatch() {
        return mismatch;
    }

    public void setMismatch(int mismatch) {
        this.mismatch = mismatch;
    }

    public float getCoverage() {
        return coverage;
    }

    public void setCoverage(float coverage) {
        this.coverage = coverage;
    }

    public int getGaps() {
        return gaps;
    }

    public void setGaps(int gaps) {
        this.gaps = gaps;
    }

    public List<Integer> getqDistance() {
        return qDistance;
    }

    public void setQregions(List<Integer> qDistance) {
        this.qDistance = qDistance;
    }

    public List<Integer> getgDistances() {
        return gDistances;
    }

    public void setSregions(List<Integer> gDistances) {
        this.gDistances = gDistances;
    }

    @Override
    public String toString() {
        return qid + '\t' +
                length + '\t' +
                sid + '\t' +
                strand + '\t' +
                score + '\t' +
                mismatch + '\t' +
                gaps + '\t' +
                qDistance + '\t' +
                gDistances + '\t' +
                coverage;
    }
}