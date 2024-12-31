def call(Map configMap){    // call() will work like main() method in java
    // keep/place the complete pipeline code here: backend-jenkins-k8s --> Jenkinsfile.bkp 
    echo "this msg from nodejsEKS.grrovy --> call() method..."
    pipeline {
        agent {
            label 'AGENT-1'
        }
        options {
            //timeout(time: 5, unit: 'SECONDS')
            timeout(time: 30, unit: 'MINUTES')
            disableConcurrentBuilds()  // it will not allow concurrent builds
            ansiColor('xterm')
        }
        environment{
            def appVersion = '' //variable declaration
            nexusUrl = pipelineGlobals.nexusURL()
            region = pipelineGlobals.region()
            account_id = pipelineGlobals.account_id()     // account_id is aws account user id(user specific)
            component = configMap.get("component")
            project = configMap.get("project")
            def releaseExists = ""
        }
        stages {

            // stage('test'){
            //     steps{
            //         sh """
            //         echo 'this is sample testing stage'
            //         ls -ltr

            //         """
            //     }
            // }


            stage('read the version'){
                steps{
                    script{
                        def packageJson = readJSON file: 'package.json'
                        appVersion = packageJson.version
                        echo "application version: $appVersion"
                    }
                }
            }
            stage('Install Dependencies') {
                steps {
                    sh """
                    echo 'Hi, this is to install npm dependencies'
                    npm install
                    ls -ltr
                    """
                }
            }
            stage('Build'){
                steps{
                    sh """
                    zip -q -r ${component}-${appVersion}.zip * -x Jenkinsfile -x ${component}-${appVersion}.zip
                    ls -ltr
                    """
                }
            }
            ////ex: zip -q -r backend-1.0.0.zip * -x Jenkinsfile -x backend-1.0.0.zip
            // -q (quiet)is used to not to show the unnecessary console logs while performing zip action/task
            // -x is used to exclude that specific file from zip in that directory.

            stage('Docker build'){
                steps{
                    sh """
                        aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${account_id}.dkr.ecr.${region}.amazonaws.com

                        docker build -t ${account_id}.dkr.ecr.${region}.amazonaws.com/${project}-${component}:${appVersion} .

                        docker push ${account_id}.dkr.ecr.${region}.amazonaws.com/${project}-${component}:${appVersion}
                    """
                }
            }
            stage('Deploy'){
                steps{
                    sh """
                                 aws eks update-kubeconfig --region ${region} --name ${project}-dev
                                 cd helm
                                 sed -i 's/IMAGE_VERSION/${appVersion}/g' values.yaml
                                 helm uninstall ${component}
                             """
                    
                    // script{
                    //      releaseExists = sh(script: "helm list -A --short | grep -w ${component} || true", returnStdout: true).trim()
                    //     if(releaseExists.isEmpty()){
                    //         echo "${component} not installed yet, first time installation"
                    //         sh """
                    //             aws eks update-kubeconfig --region ${region} --name ${project}-dev
                    //             cd helm
                    //             sed -i 's/IMAGE_VERSION/${appVersion}/g' values.yaml
                    //             helm install ${component} -n ${project} .
                    //         """
                    //     }
                    //     else{
                    //         echo "${component} exists, running upgrade"
                    //         sh """
                    //             aws eks update-kubeconfig --region ${region} --name ${project}-dev
                    //             cd helm
                    //             sed -i 's/IMAGE_VERSION/${appVersion}/g' values.yaml
                    //             helm upgrade ${component} -n ${project} .
                    //         """

                    //     }
                    // }
                }
                // sed -i 's/IMAGE_VERSION/${appVersion}/g' values.yaml ---> it will replace "IMAGE_VERSION" string with the value in {appVersion} inside values.yaml file.
                // helm install backend . ---> use for the 1st time installation
                // helm upgrade backend . ---> from the next installation onwards(after 1st installation)
            }

    /*         stage('Nexus Artifact Upload'){
                steps{
                    echo 'Hi, this is Nexus Artifact Uploading stage'
                    script{
                        nexusArtifactUploader(
                            nexusVersion: 'nexus3',
                            protocol: 'http',
                            nexusUrl: "${nexusUrl}",
                            groupId: 'com.expense',
                            version: "${appVersion}",
                            repository: "backend",
                            credentialsId: 'nexus-auth',
                            artifacts: [
                                [artifactId: "backend" ,
                                classifier: '',
                                file: "backend-" + "${appVersion}" + '.zip',
                                type: 'zip']
                            ]
                        )
                    }
                }
            } */

        }

        post { 
            always { 
                echo 'I will always say Hello again!'
                deleteDir()     // workspace has to be deleted after every build to avoid issues for next builds
            }
            success { 
                echo 'I will say Hello only when it is success!'
            }
            failure { 
                echo 'I will say Bye when it is failed!'
            }
        }

    }

}
