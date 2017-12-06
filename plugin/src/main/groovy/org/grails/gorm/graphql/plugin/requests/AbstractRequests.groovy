package org.grails.gorm.graphql.plugin.requests

import graphql.ExecutionResult
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.plugin.GraphQLRequests

@CompileStatic
abstract class AbstractRequests implements GraphQLRequests{
    String view
    int counter=-1
    int size
    
    AbstractRequests(String view,int size=1){
        this.view=view
        this.size=size
    }

    @Override
    boolean hasNext() {
        ++counter < size
    }

    @Override
    void setResult(ExecutionResult executionResult) {
        Map<String, Object> result = new LinkedHashMap<>()
        if (executionResult.errors.size() > 0) {
            result.put('errors', executionResult.errors)
        }
        result.put('data', executionResult.data)
        setModel(result)
    }
    abstract void setModel(Map model)    
}
