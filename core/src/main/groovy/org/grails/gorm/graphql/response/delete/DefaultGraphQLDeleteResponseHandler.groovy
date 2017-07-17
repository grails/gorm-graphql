package org.grails.gorm.graphql.response.delete

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLObjectType.newObject
import graphql.schema.GraphQLNonNull
import graphql.Scalars
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.response.CachingGraphQLResponseHandler

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

    @Override
    GraphQLObjectType getObjectType() {
        definition
    }

    protected List<GraphQLFieldDefinition> buildFieldDefinitions() {
        [newFieldDefinition().name('success').type(GraphQLNonNull.nonNull(Scalars.GraphQLBoolean)).build()]
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
