# Server


## Authors

Group A68

### Lead developer 

89517 [Pedro Galhardo](mailto:pedro.galhardo@tecnico.ulisboa.pt) @ist189517

### Contributors

86391 [Beatriz Alves](mailto:beatriz.alves@tecnico.ulisboa.pt) @ist186391

89415 [António Lopes](mailto:antoniocarlosptsl@tecnico.ulisboa.pt) @ist189415

## About

This is a gRPC server defined by the protobuf specification.

The server runs in a stand-alone process.

This is the record server, where the all the volatile data is stored. It can be loaded from a *.csv* file or created without initialization. 

During execution, it will receive calls from the *hub*, to get or store data. 

It should be the first module launched.


## Instructions for using Maven

To compile and run:

```
mvn compile exec:java
```

When running, the server awaits connections from *hub* clients.

The launch flags and arguments are defined in the POM.

Alternatively, the server can be launched using appassembler plugin on Linux::

```
./target/appassembler/bin/rec <zookeeper_address> <zookeeper_port> <rec_address> <rec_port> <instance>
```

To launch using appassembler plugin on Windows:
```
target\appassembler\bin\rec <zookeeper_address> <zookeeper_port> <rec_address> <rec_port> <instance>
```

## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

