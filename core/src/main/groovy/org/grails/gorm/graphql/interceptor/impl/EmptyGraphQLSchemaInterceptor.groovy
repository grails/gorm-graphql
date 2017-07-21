package org.grails.gorm.graphql.interceptor.impl

import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.interceptor.GraphQLSchemaInterceptor

/**
 * Default implementation of {@link GraphQLSchemaInterceptor} that
 * has no operations.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class EmptyGraphQLSchemaInterceptor implements GraphQLSchemaInterceptor {

    @Override
    void interceptEntity(PersistentEntity entity, List<GraphQLFieldDefinition.Builder> queryFields, List<GraphQLFieldDefinition.Builder> mutationFields) {
        //no-op
    }

    @Override
    void interceptSchema(GraphQLObjectType.Builder queryType, GraphQLObjectType.Builder mutationType) {
        //no-op
    }
}
