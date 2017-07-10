package org.grails.gorm.graphql.fetcher

import grails.gorm.transactions.Transactional
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler

/**
 * A class for deleting entities with GraphQL
 *
 * @param <T> The domain type to delete
 * @author James Kleeh
 */
@CompileStatic
class DeleteEntityDataFetcher<T> extends GormDataFetcher<T> {

    GraphQLDeleteResponseHandler responseHandler

    DeleteEntityDataFetcher(PersistentEntity entity, GraphQLDeleteResponseHandler responseHandler) {
        super(entity)
        this.responseHandler = responseHandler
    }

    @Override
    @Transactional
    T get(DataFetchingEnvironment environment) {
        GormEntity instance = queryInstance(environment)

        boolean success = false
        try {
            ((GormEntity)instance).delete(failOnError: true)
            success = true
        } catch (e) {}

        (T)responseHandler.createResponse(environment, success)
    }

}