package org.grails.gorm.graphql.response.delete

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLObjectType

/**
 * Responsible for determining the data available in a GraphQL delete mutation response
 *
 * @author James Kleeh
 * @since 1.0.0
 */
interface GraphQLDeleteResponseHandler {

    GraphQLObjectType getObjectType()

    Object createResponse(DataFetchingEnvironment environment, boolean success)
}
