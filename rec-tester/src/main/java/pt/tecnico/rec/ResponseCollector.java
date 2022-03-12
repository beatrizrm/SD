package pt.tecnico.rec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResponseCollector<T> {
    private List<Response<T>> responses = new ArrayList<>();
    private volatile int numResponses = 0;

    public synchronized void addResponse(int instance, T content) {
        responses.add(new Response<T>(instance, content));
        numResponses++;
    }

    public synchronized void addResponse(int instance, T content, int seq, int cid) {
        responses.add(new Response<T>(instance, content, seq, cid));
        numResponses++;
    }

    public synchronized List<Response<T>> getResponses() {
        return responses;
    }

    public synchronized int getNumResponses() {
        return numResponses;
    }

    public T getQuorumResponse() {
        Comparator<Response<T>> byTag = (r1, r2) -> r1.getTag().compareTo(r2.getTag());
		return Collections.max(responses, byTag).getContent();
    }

    public void sortByInstance() {
        Collections.sort(responses, Comparator.comparing(d -> d.getInstance()));
    }
}
