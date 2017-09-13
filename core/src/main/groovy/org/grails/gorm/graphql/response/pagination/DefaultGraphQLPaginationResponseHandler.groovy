package org.grails.gorm.graphql.response.pagination

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLList
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLObjectType.newObject

/**
 * Controls how a page of results are returned
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DefaultGraphQLPaginationResponseHandler implements GraphQLPaginationResponseHandler {

    private GraphQLObjectType cachedObjectType
    protected String resultsField = 'results'
    protected String totalField = 'totalCount'

    protected List<GraphQLFieldDefinition> getFields(GraphQLOutputType resultsType, GraphQLTypeManager typeManager) {
        [newFieldDefinition()
                .name(resultsField)
                .type(GraphQLList.list(resultsType))
                .build(),
         newFieldDefinition()
                .name(totalField)
                .type((GraphQLOutputType)typeManager.getType(Long))
                .build()]
    }

    @Override
    GraphQLObjectType getObjectType(PersistentEntity entity, GraphQLTypeManager typeManager) {
        if (cachedObjectType == null) {
            GraphQLOutputType resultsType = typeManager.getQueryType(entity, GraphQLPropertyType.OUTPUT)
            cachedObjectType = newObject()
                .name(typeManager.namingConvention.getPagination(entity))
                .fields(getFields(resultsType, typeManager))
                .build()
        }
        cachedObjectType
    }

    @Override
    Object createResponse(DataFetchingEnvironment environment, PaginationResult result) {
        Map response = new LinkedHashMap<String, Object>(2)
        response.put(resultsField, result.results)
        response.put(totalField, result.totalCount)
        response
    }

    @Override
    int getDefaultMax() {
        100
    }

    @Override
    int getDefaultOffset() {
        0
    }
}
