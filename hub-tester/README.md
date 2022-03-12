# Tester


## Authors

Group A68

### Lead developer 

89517 [Pedro Galhardo](mailto:pedro.galhardo@tecnico.ulisboa.pt) @ist189517

### Contributors

89415 [António Lopes](mailto:antoniocarlosptsl@tecnico.ulisboa.pt) @ist189415

86391 [Beatriz Alves](mailto:beatriz.alves@tecnico.ulisboa.pt) @ist186391


## About

This is a gRPC client that performs integration tests on the running *hub* server.
The integration tests verify the responses of the server to a set of requests.

The *rec* server needs to be running as well for the tests to work.

The tests contain the case for sucess and several cases to check for the exceptions thrown.


## Instructions for using Maven

You must start the servers first.

To compile and run integration tests:

```
mvn verify
```


## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

