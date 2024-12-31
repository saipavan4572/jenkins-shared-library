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
        parameters{
            // which component you want to deploy
            // which environment
            // which version
        }
        stages {

            stage('Deploy'){
                steps{
                    script{
                        // deploy to specific environment like QA, UAT, PERF, etc.
                    }
                }
            }
            stage('Integrations tests') {
                steps {
                    // Run integration tests
                }
            }

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

