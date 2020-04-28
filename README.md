### What is KafkaLagReporter?

KafkaLagReporter is an application written in SpringBoot to expose the Lag created at the kafka partitions because of difference
in the speed at which Kafka producer and Kafka consumer. 

When Kafka topic/partition is created, two offset are created.
1. Current offset: Last location where message has been placed
2. Last committed offset: Last location up to which consumer has read the message.

But many times, when the producer is very fast or consumer is slow, the current offset and the last committed offset differs. The difference between
current offset and last committed offset is called lag.

Currently, using Jolokia and JMX we can expose Kafka metrics but those metrics do not include consumer lag information.  

## How KafkaLagReporter works?
This application reads the configuration from file whose path is specified in `CONFIG_FILE_PATH` or it reads from the environment variable.
If values are present at both the places, value present at environment value will be given higher precedence.

Note: `CONFIG_FILE_PATH` should be the absolute location. 

# Properties List 

These properties can be mentioned in `CONFIG_FILE_PATH` or passed as environment variable.

```
GROUPS=<group1>|<group2>
BOOTSTRAP_SERVER=localhost:9092
KAFKA_LOCATION=/opt/kafka
PERIOD=300 #In seconds
WAVEFRONT_REPORTING_ENABLED=true
WAVEFRONT_REPORTER_PREFIX=<wf-reporter-prefix>
WAVEFRONT_HOST=<wavefront-proxy-url>
WAVEFRONT_PORT=<wf-port>
```

Note: Please take care of the values properly otherwise the wrong metrics will be reported or won't be reported at all. 

# How it reports the metrics?

KafkaLagReporter exports the metrics in two ways.
1. Using the JMX beans.
2. Using wavefront proxy.

Although the metrics is registered in the JMX beans, in the docker image creation we have not specified Jolokia.
So, if anyone is interested they can get the metrics from JMX through Jolokia but they will have to put jolokia 
and expose the port.

Using wavefront proxy is a neat approach.So, we have enabled that.
But if someone is not interested in exposing the metrics to wavefront. 
They can disable this feature by making `WAVEFRONT_REPORTING_ENABLED` as `false`.


# How to make a build?

We need to create a build only of there is any change in the code has been done.
Set the new version in the pom.xml and run below commands

```
// Set the CONFIG_FILE_PATH to the location where the file exists inside the src/main/resources folder.
// It is used only to create a build.
export CONFIG_FILE_PATH=<config-file-path>
mvn clean install
```
