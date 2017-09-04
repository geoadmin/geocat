#!/usr/bin/groovy

@Library('c2c-pipeline-library') import static com.camptocamp.utils.*

selectNodes {
  (it.memorysize_mb as Float) > 12000
}

def spawnContainer(def containerName, def containerImage) {
   sh "docker run -it -d -v `pwd`:/home/build --name ${containerName} -w /home/build ${containerImage} /bin/bash"
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

  def mavenOpts = '-B -Dmaven.repo.local=./.m2_repo -Ddb.username=geonetwork -Ddb.name=geonetwork -Ddb.type=postgres -Ddb.host=database -Ddb.password=geonetwork'

  stage('docker pull') {
    sh "docker pull ${mavenContainerImage}"
    sh "docker pull ${terraformContainerImage}"
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
    executeInContainer(buildContainerName, "mvn clean install ${mavenOpts} -DskipTests")
  }
  stage('Second build with tests') {
    executeInContainer(buildContainerName,"mvn clean install ${mavenOpts} -fae")
  }
  stage('calculating coverage') {
    executeInContainer(buildContainerName, "mvn cobertura:cobertura ${mavenOpts} -fae -Dcobertura.report.format=xml")
    step([$class: 'CoberturaPublisher',
        autoUpdateHealth: false,
        autoUpdateStability: false,
        coberturaReportFile: '**/target/site/cobertura/coverage.xml',
        failNoReports: true,
        failUnhealthy: false,
        failUnstable: false,
        maxNumberOfBuilds: 0,
        onlyStable: false,
        sourceEncoding: 'UTF_8',
        zoomCoverageChart: true])
  }
  stage('Saving tests results') {
    // This task does not need to run in the builder container
    junit '**/target/surefire-reports/TEST-*.xml'
  }
  stage('configure georchestra c2c docker-hub account') {
    // Requires a username / password configured in Jenkins' credentials, with id docker-c2cgeorchestra
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'dockerhub',
        usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
      def configXmlStr = """<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
        http://maven.apache.org/xsd/settings-1.0.0.xsd">
        <servers>
        <server>
        <id>docker-hub</id>
        <username>USERNAME</username>
        <password>PASSWORD</password>
        <configuration>
        <email>geocat2@camptocamp.com</email>
        </configuration>
        </server>
        </servers>
        </settings>""".replaceAll("USERNAME", env.USERNAME).replaceAll("PASSWORD", env.PASSWORD)
      executeInContainer(buildContainerName, "echo '${configXmlStr}' > /settings.xml")
    }
  }
  stage('Build/publish a docker image') {
    // one-liner to setup docker
    executeInContainer(buildContainerName, "curl -fsSLO https://get.docker.com/builds/Linux/x86_64/docker-17.05.0-ce.tgz && tar --strip-components=1 -xvzf docker-17.05.0-ce.tgz -C /usr/local/bin")
    executeInContainer(buildContainerName, "mvn -s /settings.xml ${mavenOpts} -pl web -Pdocker docker:build docker:push")
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
        sh "docker cp ${FILE} ${deployContainerName}:/root/.rancher/caas.dev.bgdi.ch.json"
      } // withCredentials
    } // stage

    stage("Configuring AWS / S3") {
        withCredentials([[$class: 'UsernamePasswordMultiBinding',
            credentialsId: 'terraform-georchestra-aws-credentials',
            usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
          def credentialsFile = """
[c2c]
aws_access_key_id = ${env.USERNAME}
aws_secret_access_key = ${env.PASSWORD}
region = eu-west-1
"""
          executeInContainer(deployContainerName, "echo '${credentialsFile}' > ~/.aws/credentials"
        } // withCredentials
    } // stage

    stage('Checking out the terraform-geocat repository') {
      withCredentials([file(credentialsId: 'terraform-geocat-deploy-key', variable: 'FILE')]) {
        sh "docker cp ${FILE} ${deployContainerName}:/ssh-github-access"
        executeInContainer(deployContainerName, "rm -rf terraform-geocat")
        executeInContainer(deployContainerName, "ssh-agent /bin/bash -c "ssh-add /ssh-github-access ; ssh -oStrictHostKeyChecking=no git@github.com || true ; git clone git@github.com:camptocamp/terraform-geocat.git"
      }
    } // stage

    stage('Terraforming') {
        if (env.BRANCH_NAME == 'geocat_3.4.x') {
          executeInContainer(deployContainerName, """cd terraform-geocat                        &&
            ln -s /root/bin/terraform /usr/bin             &&
            make install                                   &&
            make init                                      &&
            cd rancher-environments/geocat-dev             &&
            terraform apply""")
        } else {
          println "Not onto the 'geocat_3.4.x' branch, skipping redeploy"
        }// if
    } // stage

    stage ('Destroy deployer container') {
       destroyContainer(deployContainerName)
    }
  } // stage
} // dockerBuild
