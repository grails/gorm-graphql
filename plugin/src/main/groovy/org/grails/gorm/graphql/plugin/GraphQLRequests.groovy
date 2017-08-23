package org.grails.gorm.graphql.plugin

import graphql.ExecutionResult

interface GraphQLRequests extends Iterator<GraphQLRequest>{
    boolean validate()
    void setResult(ExecutionResult executionResult)
    
    String getView()
    Map getModel()
}