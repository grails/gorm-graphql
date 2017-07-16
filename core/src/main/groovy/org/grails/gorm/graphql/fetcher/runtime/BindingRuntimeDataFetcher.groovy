package org.grails.gorm.graphql.fetcher.runtime

import graphql.schema.DataFetcher
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.binding.manager.GraphQLDataBinderManager
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager

/**
 * A runtime data fetcher implementation used for fetchers
 * that require data binding
 *
 * @see AbstractRuntimeDataFetcher
 *
 * @param <T> The domain type to return
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class BindingRuntimeDataFetcher<T> extends AbstractRuntimeDataFetcher<T> {

    GraphQLDataBinderManager binderManager
    GraphQLDataFetcherType type
    PersistentEntity entity

    BindingRuntimeDataFetcher(PersistentEntity entity,
                              GraphQLDataFetcherManager fetcherManager,
                              GraphQLDataBinderManager binderManager,
                              GraphQLDataFetcherType type) {
        super(fetcherManager)
        this.binderManager = binderManager
        this.type = type
        this.entity = entity
    }

    @Override
    DataFetcher resolveFetcher() {
        manager.getBindingFetcher(entity, binderManager.getDataBinder(entity.javaClass), type)
    }
}
