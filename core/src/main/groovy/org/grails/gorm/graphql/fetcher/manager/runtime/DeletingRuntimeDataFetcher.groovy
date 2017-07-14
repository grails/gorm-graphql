package org.grails.gorm.graphql.fetcher.manager.runtime

import graphql.schema.DataFetchingEnvironment
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler

/**
 * Created by jameskleeh on 7/14/17.
 */
class DeletingRuntimeDataFetcher<T> extends AbstractRuntimeDataFetcher<T> {

    GraphQLDeleteResponseHandler responseHandler
    PersistentEntity entity

    DeletingRuntimeDataFetcher(PersistentEntity entity,
                               GraphQLDataFetcherManager manager,
                               GraphQLDeleteResponseHandler responseHandler) {
        super(manager)
        this.responseHandler = responseHandler
        this.entity = entity
    }

    @Override
    T get(DataFetchingEnvironment environment) {
        return (T)manager.getDeletingFetcher(entity, responseHandler).get(environment)
    }
}
