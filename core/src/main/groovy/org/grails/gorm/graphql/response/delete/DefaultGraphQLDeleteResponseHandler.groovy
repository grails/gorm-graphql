package org.grails.gorm.graphql.response.delete

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.response.CachingGraphQLResponseHandler
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLObjectType.newObject

/**
 * The default data available in a delete mutation response
 *
 * success: Boolean
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DefaultGraphQLDeleteResponseHandler extends CachingGraphQLResponseHandler implements GraphQLDeleteResponseHandler {

    protected String description = 'Whether or not the operation was successful'
    protected String name = 'DeleteResult'

    protected GraphQLTypeManager typeManager

    DefaultGraphQLDeleteResponseHandler(GraphQLTypeManager typeManager) {
        this.typeManager = typeManager
    }

    @Override
    GraphQLObjectType getObjectType() {
        definition
    }

    protected List<GraphQLFieldDefinition> buildFieldDefinitions() {
        [newFieldDefinition().name('success').type((GraphQLOutputType)typeManager.getType(Boolean, false)).build()]
    }

    @Override
    protected GraphQLObjectType buildDefinition() {
        newObject()
            .name(name)
            .description(description)
            .fields(buildFieldDefinitions())
            .build()
    }

    @Override
    Object createResponse(DataFetchingEnvironment environment, boolean success) {
        [success: success]
    }
}
