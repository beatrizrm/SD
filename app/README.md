# Application


## Authors

Group A68

### Lead developer 

86391 [Beatriz Alves](mailto:beatriz.alves@tecnico.ulisboa.pt) @ist186391

### Contributors

89415 [António Lopes](mailto:antoniocarlosptsl@tecnico.ulisboa.pt) @ist189415

89517 [Pedro Galhardo](mailto:pedro.galhardo@tecnico.ulisboa.pt) @ist189517

## About

This is a CLI (Command-Line Interface) application.


## Instructions for using Maven

To compile and run using _exec_ plugin:

```
mvn compile exec:java
```

To generate launch scripts for Windows and Linux
(the POM is configured to attach appassembler:assemble to the _install_ phase):

```
mvn install
```

To run using appassembler plugin on Linux:

```
./target/appassembler/bin/app <zookeeper_host> <zookeeper_port> <user_id> <phone_number> <latitude> <longitude>
```

To run using appassembler plugin on Windows:

```
target\appassembler\bin\app <zookeeper_host> <zookeeper_port> <user_id> <phone_number> <latitude> <longitude>
```


## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

