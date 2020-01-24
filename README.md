# cloudio-endpointExample-java

This repository give a few example on how to use the cloud.iO java library. 

## Requirement

You'll need cloud.iO endpoint certificate, those are available through cloud.iO RESTfull API, contact your cloud.iO administrator if don't know how to retreive them.

You'll have to have the following files in the resources/cloud.io directory accoarding to your own "endpointUUID":
  - "endpointUUID".properties --> can follow the given example
  - "endpointUUID".p12
  - ca-cert.jks

You'll also have to modify the initialisation of the Endpoints with your own enpointUUID in each Application.java example file in each package.

## Packages

### [basicDataModel](https://github.com/lucblender/cloudio-endpointExample-java/tree/master/src/main/java/basicDataModel)

Two cloud.iO ressources implementation example can be found here:

- [DemoNode](../master/src/main/java/basicDataModel/DemoNode.java)
  - having a DemoObject
- [DemoObject](../master/src/main/java/basicDataModel/DemoObject.java)
  - having all possible type of attribute constraint of cloud.iO
  
If you add the DemoNode to your Endpoint, its structure will be the following:

```
Endpoint
`-- DemoNode
    `-- DemoNode
        |-- demoStatic
        |-- demoMeasure
        |-- demoParameter
        |-- demoStatus
        `-- demoSetpoint
```


### [basic](https://github.com/lucblender/cloudio-endpointExample-java/tree/master/src/main/java/basic)

This package will simply use the structure from [basicDataModel](https://github.com/lucblender/cloudio-endpointExample-java/tree/master/src/main/java/basicDataModel), adding listener to demoParameter and demoSetPoint, and update demoStatus and demoMeasure every second. 

### [endpointListener](https://github.com/lucblender/cloudio-endpointExample-java/tree/master/src/main/java/endpointListener)

Basic implementation of an CloudioEndpointListener allowing the developper to know when endpoint is online or offline.

### [nodeInterfaceObjectClass](https://github.com/lucblender/cloudio-endpointExample-java/tree/master/src/main/java/nodeInterfaceObjectClass)
Example of Node Interface and Object class as it could be implemented as a library. Use of the @Implements for Node Interface and the @Conforms for Object class.

cloud.iO support multiple node interface, in java this cannot be achieve with multiple interface since their attribute are final static and neither with multiple inherance since it's not supported by java... So we can only use inherance for if we create a Node implmenting one interface as seen with [DemoNodeOneInterface](../master/src/main/java/nodeInterfaceObjectClass/DemoNodeOneInterface.java). For multiple interfaces, it is up to the user to do it's own implemntation "by hand" as seen in [DemoNodeMultipleInterfaces](../master/src/main/java/nodeInterfaceObjectClass/DemoNodMultipleInterfaces.java).

### [transaction](https://github.com/lucblender/cloudio-endpointExample-java/tree/master/src/main/java/transaction)

Simple example of use of transaction using the structure from [basicDataModel](https://github.com/lucblender/cloudio-endpointExample-java/tree/master/src/main/java/basicDataModel).

### [logging](https://github.com/lucblender/cloudio-endpointExample-java/tree/master/src/main/java/logging)

Example of logging with log4j, logs are directly push to cloud.iO thanks to the endpoint library. On top of this configuration we can create a [log4j2.xml](../master/src/main/resources/log4j2.xml) file configuration to add more appender.

### [reactivity](https://github.com/lucblender/cloudio-endpointExample-java/tree/master/src/main/java/reactivity)

Will send @update regularly subscribe to the same and do rest request on notify attribute change to compute the delay of time for the message to come to the cloud and come back.

After a few message, will stop, compute the min, max, mean and standard deviation of the measured times and also output two histograms of those delays. 

