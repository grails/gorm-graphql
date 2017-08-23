package org.grails.gorm.graphql.plugin

import grails.util.TypeConvertingMap
import grails.web.mime.MimeType
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.plugin.requests.BatchGraphQLRequests
import org.grails.gorm.graphql.plugin.requests.SingleGraphQLRequest

@CompileStatic
class GraphQLRequestUtils {

    static GraphQLRequests graphQLRequestWithParams(GrailsParameterMap params) {
        GraphQLRequest graphQLRequest = new GraphQLRequest()
        graphQLRequest.query = params.query
        graphQLRequest.operationName = params.containsKey('operationName') ? params.operationName : null
        if (params.containsKey('variables')) {
            graphQLRequest.variables = new JsonSlurper().parseText(params.variables as String) as Map
        } else {
            graphQLRequest.variables = Collections.emptyMap()
        }
        new SingleGraphQLRequest(graphQLRequest)
    }

    static GraphQLRequests graphQLRequestWithBodyAndMimeTypes(String body, MimeType[] mimeTypes) {
        if (mimeTypes == null) {
            return null
        }

        if (mimeTypes.contains(MimeType.JSON)) {
            return graphQLRequestWithJSONBody(body)
        }

        if (mimeTypes.contains(GormGraphqlGrailsPlugin.GRAPHQL_MIME)) {
            return graphQLRequestWithGraphqlBody(body)
        }

        return null
    }

    static GraphQLRequests graphQLRequestWithJSONBody(String body) {
        Object json = new JsonSlurper().parseText(body)
        
        if(json instanceof Map){
            new SingleGraphQLRequest(graphQLRequestWithMap((Map)json))            
        }
        else{
            new BatchGraphQLRequests(((Collection)json).collect{graphQLRequestWithMap((Map)it)} as GraphQLRequest[])            
        }
    }
    static GraphQLRequest graphQLRequestWithMap(Map data) {
        TypeConvertingMap json = new TypeConvertingMap(data)
        GraphQLRequest graphQLRequest = new GraphQLRequest()
        graphQLRequest.with {
            query = json.query.toString()
            operationName = json.containsKey('operationName') ? json.operationName : null
            variables = (json.variables instanceof Map) ? json.variables as Map : Collections.emptyMap()
        }
        graphQLRequest
    }
    static GraphQLRequests graphQLRequestWithGraphqlBody(String body) {
        GraphQLRequest graphQLRequest = new GraphQLRequest()
        graphQLRequest.with {
            query = body
            operationName = null
            variables = Collections.emptyMap()
        }
        new SingleGraphQLRequest(graphQLRequest)
    }
}
