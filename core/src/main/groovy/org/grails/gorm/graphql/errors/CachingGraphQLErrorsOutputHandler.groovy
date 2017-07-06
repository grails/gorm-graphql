package org.grails.gorm.graphql.errors

import graphql.schema.GraphQLObjectType

/**
 * Created by jameskleeh on 6/6/17.
 */
abstract class CachingGraphQLErrorsOutputHandler implements GraphQLErrorsOutputHandler {

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
