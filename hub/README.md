# Server


## Authors

Group A68

### Lead developer 

89415 [António Lopes](mailto:antoniocarlosptsl@tecnico.ulisboa.pt) @ist189415

### Contributors

89517 [Pedro Galhardo](mailto:pedro.galhardo@tecnico.ulisboa.pt) @ist189517

86391 [Beatriz Alves](mailto:beatriz.alves@tecnico.ulisboa.pt) @ist186391


## About

This is a gRPC server defined by the protobuf specification.

The server runs in a stand-alone process.


## Instructions for using Maven

To compile and run:

```
mvn compile exec:java
```

When running, the server awaits connections from *app* clients.

The launch flags and arguments are defined in the POM.

Alternatively, to run using appassembler plugin on Linux:

```
./target/appassembler/bin/hub <zookeeper_address> <zookeeper_port> <hub_address> <hub_port> <instance> <users_file> <stations_file> [initRec]
```

The second server can be launched using appassembler plugin on Linux:

```
./target/appassembler/bin/hub <zookeeper_address> <zookeeper_port> <hub_address> <hub_port> <instance> <users_file> <stations_file>
```

To run using appassembler plugin on Windows:

```
target\appassembler\bin\hub <zookeeper_address> <zookeeper_port> <hub_address> <hub_port> <instance> <users_file> <stations_file> [initRec]
```

The second server can be launched using appassembler plugin on Windows:

```
target\appassembler\bin\hub <zookeeper_address> <zookeeper_port> <hub_address> <hub_port> <instance> <users_file> <stations_file>
```


## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

