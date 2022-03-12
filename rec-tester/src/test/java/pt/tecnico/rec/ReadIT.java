package pt.tecnico.rec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.tecnico.rec.grpc.*;

public class ReadIT extends BaseIT {
    
    ReadRequest request;
    ReadResponse response;

    @BeforeEach
    public void setUp ()  {
        // read a record before each test (which creates a new record with default value "0")
        request = ReadRequest.newBuilder().setId("test").build();
        response = frontend.read(request);
    }

    @Test
    public void ReadOKTest() {
        // check for correct response
        assertEquals("0", response.getValue());
    }

    @Test
    public void ReadUpdateOKTest() {
        // update an existing record
        WriteRequest wrequest = WriteRequest.newBuilder().setId("test").setValue("updated").build();
        WriteResponse wresponse = frontend.write(wrequest);
        assertEquals("OK", wresponse.getResponse());
        // check if record was updated
        request = ReadRequest.newBuilder().setId("test").build();
        response = frontend.read(request);
        assertEquals("updated", response.getValue());
    }

    @AfterEach
    public void cleanUp() {
        WriteRequest request = WriteRequest.newBuilder().setId("reset").build();
        WriteResponse response = frontend.write(request);
    }
}
