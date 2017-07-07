package org.grails.gorm.graphql.response

import graphql.schema.GraphQLObjectType

/**
 * Created by jameskleeh on 6/6/17.
 */
abstract class CachingGraphQLResponseHandler {

    private GraphQLObjectType _definition

    GraphQLObjectType getDefinition() {
        if (_definition != null) {
            return GraphQLObjectType.reference(_definition.name)
        }
        _definition = buildDefinition()
        _definition
    }

    abstract protected GraphQLObjectType buildDefinition()
}
