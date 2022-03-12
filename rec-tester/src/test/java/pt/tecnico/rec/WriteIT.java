package pt.tecnico.rec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.tecnico.rec.grpc.*;

public class WriteIT extends BaseIT {
    
    WriteRequest request;
    WriteResponse response;

    @BeforeEach
    public void setUp ()  {
        // write a record before each test
        request = WriteRequest.newBuilder().setId("test").setValue("created").build();
        response = frontend.write(request);
    }

    @Test
    public void WriteOKTest() {
        // check for correct response
        assertEquals("OK", response.getResponse());
        // check if record was created
        ReadRequest request = ReadRequest.newBuilder().setId("test").build();
        ReadResponse response = frontend.read(request);
        assertEquals("created", response.getValue());
    }

    @Test
    public void WriteUpdateOKTest() {
        // request to update an existing record
        request = WriteRequest.newBuilder().setId("test").setValue("updated").build();
        response = frontend.write(request);
        // check for correct response
        assertEquals("OK", response.getResponse());
        // check if record was updated
        ReadRequest request = ReadRequest.newBuilder().setId("test").build();
        ReadResponse response = frontend.read(request);
        assertEquals("updated", response.getValue());
    }

    @Test
    public void WriteResetOKTest() {
        // request to update an existing record
        request = WriteRequest.newBuilder().setId("test").setValue("updated").build();
        response = frontend.write(request);
        // check for correct response
        assertEquals("OK", response.getResponse());
        // request to reset
        request = WriteRequest.newBuilder().setId("reset").build();
        response = frontend.write(request);
        // check for correct response
        assertEquals("OK", response.getResponse());
        // check if record structure was reset
        ReadRequest request = ReadRequest.newBuilder().setId("test").build();
        ReadResponse response = frontend.read(request);
        assertEquals("0", response.getValue());
    }

    @AfterEach
    public void cleanUp() {
        WriteRequest request = WriteRequest.newBuilder().setId("reset").build();
        WriteResponse response = frontend.write(request);
    }
}
