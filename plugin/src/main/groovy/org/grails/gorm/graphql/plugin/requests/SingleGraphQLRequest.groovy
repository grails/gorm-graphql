package org.grails.gorm.graphql.plugin.requests

import groovy.transform.CompileStatic
import org.grails.gorm.graphql.plugin.GraphQLRequest

@CompileStatic
class SingleGraphQLRequest extends AbstractRequests{
    GraphQLRequest request    
    Map model
    
    SingleGraphQLRequest(GraphQLRequest request){
        super('/graphql/single')
        this.request=request
    }

    @Override
    boolean validate() {
        return request.validate()
    }    
    
    @Override
    GraphQLRequest next() {
        request
    }
}
