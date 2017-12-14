#!/usr/bin/groovy

@Library('c2c-pipeline-library') import static com.camptocamp.utils.*

selectNodes {
  (it.memorysize_mb as Float) > 12000
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

  def mavenOpts = '-B -Dmaven.repo.local=./.m2_repo -Ddb.username=db_username -Ddb.name=db_name -Ddb.type=postgres-postgis -Ddb.host=db_host -Ddb.password=db_password -Ddb.pool.maxActive=50'

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
  stage('configure georchestra c2c docker-hub account') {
    withCredentials([file(credentialsId: 'docker-maven-c2cgeorchestra', variable: 'FILE')]) {
      sh "docker cp ${FILE} ${buildContainerName}:/settings.xml"
    }
  }
  def shortCommit = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
  def dockerTag = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}-${shortCommit}"
  stage('Build/publish a docker image') {
    def dockerImageName = "camptocamp/geocat:${dockerTag}"
    // one-liner to setup docker
    executeInContainer(buildContainerName, "curl -fsSLO https://get.docker.com/builds/Linux/x86_64/docker-17.05.0-ce.tgz && tar --strip-components=1 -xvzf docker-17.05.0-ce.tgz -C /usr/local/bin")
    executeInContainer(buildContainerName, "mvn -s /settings.xml ${mavenOpts} -pl web -Pdocker -DdockerImageName=${dockerImageName} docker:build docker:push")
  }
  // at this time, the first container used to build is no longer necessary
  stage('Destroys the builder container') {
    destroyContainer(buildContainerName)
  }
  // Using another container, deploys the previously published image onto the dev env
  stage('Deploy newly created images on the dev env') {
    stage('spawning a image for deploying') {
      destroyContainer(deployContainerName)
      spawnContainer(deployContainerName, deployContainerImage)
    }
    stage('Install / configure needed tools') {
      executeInContainer(deployContainerName, 'apt-get update')
      executeInContainer(deployContainerName, 'apt-get -y install make ssh git wget unzip')
      executeInContainer(deployContainerName, 'mkdir -p /root/bin /root/.rancher /root/.aws')
    } // stage

    stage("Prepare caas-dev access") {
      withCredentials([file(credentialsId: 'jenkins-caas-dev-bgdi.ch.json', variable: 'FILE')]) {
        sh "docker cp ${FILE} ${deployContainerName}:/root/.rancher/cli-caas.dev.bgdi.ch.json"
      } // withCredentials
    } // stage

    stage("Configuring AWS / S3") {
      withCredentials([file(credentialsId: 'terraform-georchestra-aws-credentials-file', variable: 'FILE')]) {
        sh "docker cp ${FILE} ${deployContainerName}:/root/.aws/credentials"
      } //  withCredentials
    } // stage

    stage('Checking out the terraform-geocat repository') {
      sshagent(["terraform-geocat-deploy-key"]) {
        sh "rm -rf terraform-geocat"
        sh "ssh -oStrictHostKeyChecking=no git@github.com || true"
        sh "git clone git@github.com:camptocamp/terraform-geocat.git"
        sh "docker cp terraform-geocat ${deployContainerName}:/"
        sh "rm -rf terraform-geocat"
      }
    } // stage

    stage('Terraforming') {
        if (env.BRANCH_NAME.endsWith("auto-deploy") || env.BRANCH_NAME == "geocat_3.4.x") {
          executeInContainer(deployContainerName, """cd /terraform-geocat &&
            ln -s /root/bin/terraform /usr/bin             &&
            make install                                   &&
            make init                                      &&
            cd rancher-environments/geocat-dev             &&
            terraform apply -auto-approve -var geocat_tag=${dockerTag}""")
        } else {
          println "Not onto the 'geocat_3.4.x' branch, skipping redeploy"
        }// if
    } // stage

    stage ('Destroy deployer container') {
       destroyContainer(deployContainerName)
    }
  } // stage
} // dockerBuild
