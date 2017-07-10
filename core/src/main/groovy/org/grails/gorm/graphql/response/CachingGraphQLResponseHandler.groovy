package org.grails.gorm.graphql.response

import graphql.schema.GraphQLObjectType

/**
 * Generic class to cache the creation of {@link GraphQLObjectType} instances
 * by providing a reference if the object was already created when requested.
 *
 * @author James Kleeh
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
