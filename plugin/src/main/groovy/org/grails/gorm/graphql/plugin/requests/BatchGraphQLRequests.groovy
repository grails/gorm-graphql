package org.grails.gorm.graphql.plugin.requests

import groovy.transform.CompileStatic
import org.grails.gorm.graphql.plugin.GraphQLRequest

@CompileStatic
class BatchGraphQLRequests extends AbstractRequests{
    GraphQLRequest[] requests
    Map[] model 
    BatchGraphQLRequests(GraphQLRequest[] requests){
        super('/graphql/batch',requests.length)
        model = new Map[requests.length]
        this.requests=requests
    }

    @Override
    boolean validate() {
        !requests.find{!it.validate()} 
    }

    @Override
    Map getModel() {
        [data:model]
    }

    @Override
    void setModel(Map model) {
        model[counter] = model
    }

    @Override
    GraphQLRequest next() {
        return requests[counter]
    }
}
