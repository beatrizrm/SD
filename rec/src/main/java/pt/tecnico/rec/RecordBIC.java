package pt.tecnico.rec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RecordBIC {
    Map<String, String> record = new ConcurrentHashMap<String, String>();
    RecTag tag = new RecTag(0, 0);

    public RecTag getTag() {
        return tag;
    }
    public void setTag(RecTag tag) {
        this.tag = tag;
    }

    public String read(String id) {
        System.out.println("READ");
        System.out.println("  Request received: read (" + id + ")");

        if (id.equals("tag")) {
            System.out.println("  Sending tag: " + tag);
            return "";
        }
        else {
            if (!record.containsKey(id)) {
                record.put(id, "0");
            }
            System.out.println("  Sending response: " + record.get(id) + " with tag " + tag);
            return record.get(id);
        }
        
    }

    public void reset() {
        System.out.println("RESET");
        System.out.println("  Resetting data...");
        record = new ConcurrentHashMap<String, String>();
        tag = new RecTag(0, 0);
    }

    public String write(String id, String value, RecTag newTag) {
        System.out.println("WRITE");
        System.out.println("  Request received: write (" + id + ", " + value + ") with tag " + newTag);
        if (newTag.greaterThan(tag)) {
            if (id.equals("reset")) {
                reset();
            }
            else {
                record.put(id, value);
                setTag(newTag);
            }
        }
        System.out.println("  Sending response: write");
        return "OK";
    }

    public String ping() {
        System.out.println("PING");
        System.out.println("  Request received: ping");
        System.out.println("  Sending response: ping");
		return "up";
    }
}