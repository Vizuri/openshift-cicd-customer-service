FROM registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:1.6-16
# Change to latest 1.6 to correct Scanning Errors
#FROM registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:1.6
COPY target/*.jar /deployments
