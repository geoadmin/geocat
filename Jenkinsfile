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

  // database settings
  def dbName = "geonetwork"
  def dbHost = "localhost"

  def mavenOpts = "-B -Dmaven.repo.local=./.m2_repo -Ddb.name=${dbName} -Ddb.host=${dbHost} -Ddb.type=postgres-postgis -Ddb.pool.maxActive=50"

  stage('docker pull') {
    sh "docker pull ${mavenContainerImage}"
  }

  stage('Getting the sources') {
    git url: 'https://github.com/camptocamp/geocat.git', branch: env.BRANCH_NAME
    sh 'git submodule update --init --recursive'
  }
  stage('Launch the docker image needed to build') {
    destroyContainer(buildContainerName)
    spawnContainer(buildContainerName, mavenContainerImage)
  }
  // we use a credentials file to build the artifact with DB username/password
  stage('Build artifact') {
    withCredentials([usernamePassword(credentialsId: 'jenkins-geocat-mgdi-database', usernameVariable: 'DB_USERNAME', passwordVariable: 'DB_PASSWORD')]) {
      executeInContainer(buildContainerName, "MAVEN_OPTS=-Xmx8192m mvn clean install -Ddb.username=${DB_USERNAME} -Ddb.password=${DB_PASSWORD} ${mavenOpts} -fn -DskipTests")
    }
    archive 'web/target/*.war'
  }
  stage('Destroys the builder container') {
    destroyContainer(buildContainerName)
  }
} // dockerBuild
