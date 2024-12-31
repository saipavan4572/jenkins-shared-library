#!groovy

// declaring function

def decidePipeline(Map configMap) {
    type = configMap.get("type")

    echo "this msg from pipelineDecision.grrovy --> decidePipeline() method..."

    switch(type) {
        case "nodejsEKS":
            nodejsEKS(configMap)    //it will call the function call() inside nodejsEKS.groovy file.
            break
        case "nodejsVM":
            nodejsVM(configMap)
            break
        default:
            error "type is not matched"
            break
    }
}