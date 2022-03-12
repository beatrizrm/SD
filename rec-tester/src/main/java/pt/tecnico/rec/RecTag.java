package pt.tecnico.rec;

public class RecTag implements Comparable<RecTag> {
    private int seq;
    private int cid;

    public RecTag(int seq, int cid) {
        this.seq = seq;
        this.cid = cid;
    }

    public int getSeq() {
        return seq;
    }
    public void setSeq(int seq) {
        this.seq = seq;
    }
    public int getCid() {
        return cid;
    }
    public void setCid(int cid) {
        this.cid = cid;
    }

    @Override
    public int compareTo(RecTag tag2) {
        if (this.getSeq() > tag2.getSeq() || (this.getSeq() == tag2.getSeq() && this.getCid() > tag2.getCid()) ) {
            return 1; // tag1 > tag2
        }
        else if (this.getSeq() == tag2.getSeq()) {
            return 0; // tag1 == tag2
        }
        else {
            return -1; // tag1 < tag2
        }
    }

    public boolean greaterThan(RecTag tag2) {
        return this.compareTo(tag2) == 1;
    }

    @Override
    public String toString() {
        return "<" + getSeq() + ", " + getCid() + ">";
    }
}
