#!/usr/bin/groovy


def nexusUrl = "http://nexus-student-1-cicd.apps.ocp-nonprod-01.kee.vizuri.com";
def release_number;

node ("maven") {

	stage('Checkout') {
		echo "In checkout"
		checkout scm

		echo ">>>>>>  Branch Name: " + BRANCH_NAME;

		if(BRANCH_NAME.startsWith("release")) {
			def tokens = BRANCH_NAME.tokenize( '/' )
			branch_name = tokens[0]
			branch_release_number = tokens[1]

			release_number = branch_release_number
		}
		else {
			sh (
			script: "mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^\\[]' > release.txt",
			returnStdout: true,
			returnStatus: false
			)
			release_number = readFile('release.txt').trim()
			echo "release_number: ${release_number}"
		}

		
	}


	stage('Build') {
		echo "In Build"
		sh "mvn -s configuration/settings.xml -Dnexus.url=${nexusUrl}  -DskipTests=true -Dbuild.number=${release_number} clean install"
	}
	
	stage ('Unit Test') {
		sh "mvn -s configuration/settings.xml -Dnexus.url=${nexusUrl}  -Dbuild.number=${release_number} test"
		junit "target/surefire-reports/*.xml"

		step([$class: 'XUnitBuilder',
			thresholds: [
				[$class: 'FailedThreshold', unstableThreshold: '1']
			],
			tools: [
				[$class: "JUnitType", pattern: "target/surefire-reports/*.xml"]
			]])
	}
}




