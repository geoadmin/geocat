#!/usr/bin/groovy

@Library('c2c-pipeline-library') import static com.camptocamp.utils.*

selectNodes {
    (it.memorysize_mb as Float) > 12000
}

dockerBuild {
  stage('Docker pull the maven image') {
    sh 'docker pull maven:3-jdk-8'
  }
  withDockerContainer(image: 'maven:3-jdk-8') {
    stage('Getting the sources') {
      git url: 'https://github.com/camptocamp/geocat.git', branch: env.BRANCH_NAME
      sh 'git submodule update --init --recursive'
    }
    stage('First build without test') {
      sh '''mvn clean install -B -Dmaven.repo.local=./.m2_repo -Ddb.username=geonetwork -Ddb.name=geonetwork -Ddb.type=postgres -Ddb.host=database -Ddb.password=geonetwork -DskipTests'''
    }
    stage('Second build with tests') {
      sh '''mvn clean install -B -Dmaven.repo.local=./.m2_repo -Ddb.username=geonetwork -Ddb.name=geonetwork -Ddb.type=postgres -Ddb.host=database -Ddb.password=geonetwork -fn'''
    }
    stage('Saving tests results') {
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
          sh "echo '${configXmlStr}' > /settings.xml"
      }
    }
    stage('Build/publish a docker image') {
      // one-liner to setup docker
      sh 'curl -fsSLO https://get.docker.com/builds/Linux/x86_64/docker-17.05.0-ce.tgz && tar --strip-components=1 -xvzf docker-17.05.0-ce.tgz -C /usr/local/bin'
      sh '''mvn -s /settings.xml -B -Dmaven.repo.local=./.m2_repo  -Ddb.username=geonetwork -Ddb.name=geonetwork -Ddb.type=postgres -Ddb.host=database -Ddb.password=geonetwork -pl web -Pdocker docker:build docker:push'''
    }
  } // withDockerContainer

  // Using another container, deploys the previously published image onto the dev env
    stage('Deploy newly created images on the dev env') {
      withDockerContainer(image: 'debian', args: "--privileged -u 0:0") {

        stage('Install / configure needed tools') {
          sh 'apt update && apt install -y make ssh git wget unzip'
            sh 'mkdir -p ~/bin'
        } // stage

        stage("Prepare caas-dev access") {
          withCredentials([file(credentialsId: 'jenkins-caas-dev-bgdi.ch.json', variable: 'FILE')]) {
            sh 'mkdir -p ~/.rancher'
              sh "cp ${FILE} ~/.rancher/caas.dev.bgdi.ch.json"
          } // withCredentials
        } // stage

        stage("Configuring AWS / S3") {
          sh 'mkdir ~/.aws'
            withCredentials([[$class: 'UsernamePasswordMultiBinding',
                credentialsId: 'terraform-georchestra-aws-credentials',
                usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
              def credentialsFile = """
                [c2c]
                aws_access_key_id = ${env.USERNAME}
              aws_secret_access_key = ${env.PASSWORD}
              region = eu-west-1
                """
                sh "echo '${credentialsFile}' > ~/.aws/credentials"
            } // withCredentials
        } // stage

        stage('Checking out the terraform-geocat repository') {
          sshagent(["terraform-geocat-deploy-key"]) {
            sh "ssh -oStrictHostKeyChecking=no git@github.com || true"
              sh "rm -rf terraform-geocat"
              sh "git clone git@github.com:camptocamp/terraform-geocat.git"
          } // sshagent
        } // stage

        stage('Terraforming') {
          ansiColor('xterm') {
            if (env.BRANCH_NAME == 'geocat_3.4.x') {
              sh """cd terraform-geocat                        &&
                ln -s /root/bin/terraform /usr/bin             &&
                make install                                   &&
                make init                                      &&
                cd rancher-environments/geocat-dev             &&
                terraform apply"""
            } else {
              println "Not onto the 'geocat_3.4.x' branch, skipping redeploy"
            }// if
          } // ansiColor
        } // stage
      } // withDockerContainer
    } // stage
} // dockerBuild
