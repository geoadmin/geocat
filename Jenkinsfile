#!/usr/bin/groovy

@Library('c2c-pipeline-library') import static com.camptocamp.utils.*


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
      sh '''mvn clean install -B -Dmaven.repo.local=./.m2_repo -DskipTests'''
    }
    stage('Second build with tests') {
      sh '''mvn clean install -B -Dmaven.repo.local=./.m2_repo -fn'''
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
      sh '''mvn -s /settings.xml -B -Dmaven.repo.local=./.m2_repo -pl web -Pdocker docker:build docker:push'''
    }
  } // withDockerContainer
}
