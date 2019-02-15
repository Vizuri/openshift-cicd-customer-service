FROM registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:1.6-16
#FROM registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:1.4
COPY target/*.jar /deployments
