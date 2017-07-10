package org.grails.gorm.graphql.fetcher.manager

import graphql.schema.DataFetcher
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.fetcher.CreateEntityDataFetcher
import org.grails.gorm.graphql.fetcher.DeleteEntityDataFetcher
import org.grails.gorm.graphql.fetcher.EntityDataFetcher
import org.grails.gorm.graphql.fetcher.SingleEntityDataFetcher
import org.grails.gorm.graphql.fetcher.UpdateEntityDataFetcher
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler

/**
 * A default implementation of {@link GraphQLDataFetcherManager}
 *
 * @author James Kleeh
 */
@CompileStatic
class DefaultGraphQLDataFetcherManager implements GraphQLDataFetcherManager {

    @Override
    DataFetcher createGetFetcher(PersistentEntity entity) {
        new SingleEntityDataFetcher(entity)
    }

    @Override
    DataFetcher createListFetcher(PersistentEntity entity) {
        new EntityDataFetcher(entity)
    }

    @Override
    DataFetcher createCreateFetcher(PersistentEntity entity, GraphQLDataBinder dataBinder) {
        new CreateEntityDataFetcher(entity, dataBinder)
    }

    @Override
    DataFetcher createUpdateFetcher(PersistentEntity entity, GraphQLDataBinder dataBinder) {
        new UpdateEntityDataFetcher(entity, dataBinder)
    }

    @Override
    DataFetcher createDeleteFetcher(PersistentEntity entity, GraphQLDeleteResponseHandler responseHandler) {
        new DeleteEntityDataFetcher(entity, responseHandler)
    }
}
