package org.grails.gorm.graphql.types.output

import graphql.schema.GraphQLOutputType
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.response.pagination.GraphQLPaginationResponseHandler
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLObjectType.newObject

/**
 * Builds a paginated output type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class PaginatedObjectTypeBuilder implements ObjectTypeBuilder {

    GraphQLPaginationResponseHandler responseHandler
    GraphQLTypeManager typeManager

    PaginatedObjectTypeBuilder(GraphQLPaginationResponseHandler responseHandler, GraphQLTypeManager typeManager) {
        this.responseHandler = responseHandler
        this.typeManager = typeManager
    }

    @Override
    GraphQLOutputType build(PersistentEntity entity) {
        GraphQLOutputType resultsType = typeManager.getQueryType(entity, GraphQLPropertyType.OUTPUT)
        newObject()
            .name(typeManager.namingConvention.getPagination(entity))
            .description(responseHandler.getDescription(entity))
            .fields(responseHandler.getFields(resultsType, typeManager))
            .build()
    }

    @Override
    GraphQLPropertyType getType() {
        GraphQLPropertyType.OUTPUT_PAGED
    }
}
