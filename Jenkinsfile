#!/usr/bin/groovy
@Library('github.com/vizuri/openshift-pipeline-templates@master')
//@Library('gogs.apps.ocp-nonprod-01.kee.vizuri.com/student-5/openshift-cicd-pipeline@master')

def javaDeliveryPipeline = new com.vizuri.openshift.JavaDeliveryPipeline();


javaDeliveryPipeline {
	ocpAppSuffix = 'apps.ocpws.kee.vizuri.com'
	imageNamespace = 'student_1';
	registryUsername = 'student-1'
	imageBase = 'registry.kee.vizuri.com'
	registryUsername = 'student-1'
	registryPassword = 'workshop1!'
	app_name = 'customer'
	ocp_dev_cluster = 'ocp-ws'
	ocp_dev_project = 'student-1-customer-dev'
	ocp_test_cluster = 'ocp-ws'
	ocp_test_project = 'student-1-customer-test'
	ocp_prod_cluster = 'ocp-ws'
	ocp_prod_project = 'student-1-customer-prod'
}
