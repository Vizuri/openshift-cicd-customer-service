#!/usr/bin/groovy

def imageBase = "registry.kee.vizuri.com";
def imageNamespace = "student_1";
def registryUsername = "student-1"
def registryPassword = "P@ssw0rd"
def app_name = "customer";
def nexusUrl = "http://nexus-student-1-cicd.apps.ocp-nonprod-01.kee.vizuri.com";
def release_number;

def ocp_cluster = "ocp-nonprod-01"
def ocpDevProject = "student-1-customer-dev"
def ocpTestProject = "student-1-customer-test"
def ocpProdProject = "student-1-customer-prod"

def ocpAppSuffix = "apps.ocp-nonprod-01.kee.vizuri.com"


node ("maven-podman") {

	stage('Checkout') {
		echo "In checkout"
		checkout scm

		if(BRANCH_NAME ==~ /(release.*)/) {
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

	stage('SonarQube Analysis') {
		withSonarQubeEnv('sonar') { sh "mvn -s configuration/settings.xml -Dnexus.url=${nexusUrl} -Dbuild.number=${release_number}  sonar:sonar" }
	}


	stage("Quality Gate"){
		timeout(time: 1, unit: 'HOURS') {
			def qg = waitForQualityGate()
			if (qg.status != 'OK') {
				error "Pipeline aborted due to quality gate failure: ${qg.status}"
			}
		}
	}
	if (BRANCH_NAME ==~ /(develop|release.*)/) {
		def tag = "${release_number}"
		stage('Deploy Build Artifact') { sh "mvn -s configuration/settings.xml -DskipTests=true -Dbuild.number=${release_number} -Dnexus.url=${nexusUrl} deploy"	 }
		stage('Container Build') { sh "podman build -t ${imageBase}/${imageNamespace}/${app_name}:${tag} ." }
		stage('Container Push') {
			sh "podman login -u ${registryUsername} -p ${registryPassword} ${imageBase}"
			sh "podman push ${imageBase}/${imageNamespace}/${app_name}:${tag}"
		}

	}
	
}
node() {
	
	if (BRANCH_NAME ==~ /(develop)/) {
		def ocp_project = ocpDevProject;
		def tag = "${release_number}"
		stage('Container Scan') {
			checkout scm
			
			writeFile file: 'anchore_images', text: "${imageBase}/${imageNamespace}/${app_name}:${tag} Dockerfile"
			anchore engineRetries: '500', name: 'anchore_images'
		}
		stage("Deploy Openshift ${ocp_project}") {
			echo "In Deploy: ${ocp_cluster} : ${ocp_project} : ${app_name}"
			openshift.withCluster( "${ocp_cluster}" ) {
				openshift.withProject( "${ocp_project}" ) {
					def dc = openshift.selector("dc", "${app_name}")
					echo "DC: " + dc
					echo "DC Exists: " + dc.exists()
					if(!dc.exists()) {
						echo "DC Does Not Exist Creating"
						dc = openshift.newApp("-f https://raw.githubusercontent.com/Vizuri/openshift-cicd-pipeline/master/templates/springboot-dc.yaml -p IMAGE_NAME=${imageBase}/${imageNamespace}/${app_name}:${tag} -p APP_NAME=${app_name} -p DATABASE_HOST=customerdb -p DATABASE_DB=customer -p DATABASE_USER=customer -p DATABASE_PASSWORD=customer").narrow("dc")
					}
					else {
						def dcObject = dc.object()
						dcObject.spec.template.spec.containers[0].image = "${imageBase}/${imageNamespace}/${app_name}:${tag}"
						openshift.apply(dcObject)
					}

					def rm = dc.rollout()
					rm.latest()
					timeout(5) {
						def latestDeploymentVersion = openshift.selector('dc',"${app_name}").object().status.latestVersion
						echo "Got LatestDeploymentVersion:" + latestDeploymentVersion
						def rc = openshift.selector('rc', "${app_name}-${latestDeploymentVersion}")
						echo "Got RC" + rc
						rc.untilEach(1){
							def rcMap = it.object()
							return (rcMap.status.replicas.equals(rcMap.status.readyReplicas))
						}
					}
				}
			}
		}
		
		
		stage ('Integration Test') {
			def testEndpoint = "http://${app_name}-${ocp_project}.${ocpAppSuffix}"
			sh "mvn -s configuration/settings.xml -Dnexus.url=${nexusUrl} -P integration-tests -Dbuild.number=${release_number} -DbaseUrl=${testEndpoint} integration-test"
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
}




