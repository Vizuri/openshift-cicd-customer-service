#!/usr/bin/groovy

pipeline {
	def nexusUrl = "http://nexus-student-1-cicd.apps.ocp-nonprod-01.kee.vizuri.com"
	
	environment { RELEASE_NUMBER = ""; }
	node { checkout scm; }
	node ("maven") {
		echo ">>>>>>  Branch Name: " + BRANCH_NAME;
		def release_number;

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

		env.RELEASE_NUMBER = release_number;
	}
	node('maven') {
		stage('Checkout') {
			echo "In checkout"
			checkout scm
		}
		stage('Build') {
			echo "In Build"
			sh "mvn -s configuration/settings.xml -Dnexus.url=${nexusUrl}  -DskipTests=true -Dbuild.number=${env.RELEASE_NUMBER} clean install"
		}
	}
}


