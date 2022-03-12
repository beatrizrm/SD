package pt.tecnico.rec;

public class Response<T> {
    private int instance;
    private T content;
    private RecTag tag;

    public Response(int instance, T content) {
        this.instance = instance;
        this.content = content;
    }

    public Response(int instance, T content, int seq, int cid) {
        this.instance = instance;
        this.content = content;
        tag = new RecTag(seq, cid);
    }

    public int getInstance() {
        return instance;
    }

    public void setInstance(int instance) {
        this.instance = instance;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) { 
        this.content = content;
    }

    public RecTag getTag() {
        return tag;
    }
    
    public void setTag(RecTag tag) {
        this.tag = tag;
    }
}
