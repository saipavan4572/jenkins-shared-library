#!groovy

// declaring function

def decidePipeline(Map configMap) {
    type = configMap.get("type")

    echo "this msg from pipelineDecision.grrovy --> decidePipeline() method..."

    switch(type) {
        case "nodejsEKS":
            nodejsEKS(configMap)    //by default it will call the function call() inside nodejsEKS.groovy file.
            break
        case "nodejsVM":
            nodejsVM(configMap)     // if we mentioned any custom function then we can call that specific method like:nodejsVM.<methodname>
            break
        default:
            error "type is not matched"
            break
    }
}