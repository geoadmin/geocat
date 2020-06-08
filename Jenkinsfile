#!/usr/bin/groovy

@Library('c2c-pipeline-library') import static com.camptocamp.utils.*

selectNodes {
  (it.memorysize_mb as Float) > 4000
}

def spawnContainer(def containerName, def containerImage) {
   sh "docker run -it -d --privileged -v `pwd`:/home/build -v /var/run/docker.sock:/var/run/docker.sock --name ${containerName} -w /home/build ${containerImage} /bin/bash"
}

def destroyContainer(def containerName) {
    sh "docker rm -f ${containerName} || true"
}

def executeInContainer(def containerName, def cmd) {
   sh "docker exec -i ${containerName} sh -c '${cmd}'"
}

dockerBuild {
  def buildContainerName = "geocat-builder"
  def mavenContainerImage = "maven:3-jdk-8"
  def deployContainerName = "geocat-deployer"
  def deployContainerImage = "ubuntu"

  def mavenOpts = '-B -Dmaven.repo.local=./.m2_repo -Ddb.username=db_username -Ddb.name=db_name -Ddb.type=postgres-postgis -Ddb.host=db_host -Ddb.password=db_password -Ddb.pool.maxActive=50 -Ddb.port=5432'

  stage('docker pull') {
    sh "docker pull ${mavenContainerImage}"
    sh "docker pull ${deployContainerImage}"
  }

  stage('Getting the sources') {
    git url: 'https://github.com/camptocamp/geocat.git', branch: env.BRANCH_NAME
    sh 'git submodule update --init --recursive'
  }
  stage('Launch the docker image needed to build') {
    destroyContainer(buildContainerName)
    spawnContainer(buildContainerName, mavenContainerImage)
  }
  stage('First build without test') {
    executeInContainer(buildContainerName, "MAVEN_OPTS=-Xmx8192m mvn clean install ${mavenOpts} -fn -DskipTests")
  }
//  stage('Second build with tests') {
//    try {
//      executeInContainer(buildContainerName,"MAVEN_OPTS=-Xmx8192m mvn clean install ${mavenOpts} ")
//    } finally {
//      junit '**/target/surefire-reports/TEST-*.xml'
//    }
//  }
//  stage('calculating coverage') {
//    executeInContainer(buildContainerName, "MAVEN_OPTS=-Xmx8192m mvn cobertura:cobertura ${mavenOpts} -Dcobertura.report.format=xml")
//    step([$class: 'CoberturaPublisher',
//        autoUpdateHealth: false,
//        autoUpdateStability: false,
//        coberturaReportFile: '**/target/site/cobertura/coverage.xml',
//        failNoReports: true,
//        failUnhealthy: false,
//        failUnstable: false,
//        maxNumberOfBuilds: 0,
//        onlyStable: false,
//        sourceEncoding: 'UTF_8',
//        zoomCoverageChart: true])
//  }
  def shortCommit = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
  def dockerTag = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}-${shortCommit}"
  stage('Build/publish a docker image') {
    def dockerImageName = "camptocamp/geocat:${dockerTag}"
    // one-liner to setup docker
    executeInContainer(buildContainerName, "curl -fsSLO https://get.docker.com/builds/Linux/x86_64/docker-17.05.0-ce.tgz && tar --strip-components=1 -xvzf docker-17.05.0-ce.tgz -C /usr/local/bin")
    executeInContainer(buildContainerName, "mvn ${mavenOpts} -pl web -Pdocker -DdockerImageName=${dockerImageName} docker:build")
    withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                      credentialsId   : 'dockerhub',
                      usernameVariable: 'USERNAME',
                      passwordVariable: 'PASSWORD']]) {
      executeInContainer(buildContainerName, "docker login -u $USERNAME -p $PASSWORD")
      executeInContainer(buildContainerName, "docker push ${dockerImageName}")
    }
  }
  // at this time, the first container used to build is no longer necessary
  stage('Destroys the builder container') {
    destroyContainer(buildContainerName)
  }
  // Using another container, deploys the previously published image onto the dev env
} // dockerBuild
