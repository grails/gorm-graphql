package org.grails.gorm.graphql.fetcher.runtime

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.binding.DataBinderNotFoundException
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.binding.manager.GraphQLDataBinderManager
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor
import org.grails.gorm.graphql.interceptor.manager.GraphQLInterceptorManager

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

    BindingRuntimeDataFetcher(PersistentEntity entity,
                              GraphQLDataFetcherManager fetcherManager,
                              GraphQLInterceptorManager interceptorManager,
                              GraphQLDataFetcherType type,
                              GraphQLDataBinderManager binderManager) {
        super(entity, fetcherManager, interceptorManager, type)
        this.binderManager = binderManager
    }

    @Override
    DataFetcher resolveFetcher() {
        GraphQLDataBinder binder = binderManager.getDataBinder(entity.javaClass)
        if (binder == null) {
            throw new DataBinderNotFoundException(entity)
        }
        fetcherManager.getBindingFetcher(entity, binder, type)
    }

    boolean intercept(DataFetchingEnvironment environment) {
        for (GraphQLFetcherInterceptor i: interceptors) {
            if (!i.onMutation(environment, type)) {
                return false
            }
        }
        true
    }
}
