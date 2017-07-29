package org.grails.gorm.graphql.fetcher.runtime

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor
import org.grails.gorm.graphql.interceptor.manager.GraphQLInterceptorManager
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler

/**
 * A runtime data fetcher implementation used for fetchers
 * that delete
 *
 * @see AbstractRuntimeDataFetcher
 *
 * @param <T> The domain returnType to return
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DeletingRuntimeDataFetcher<T> extends AbstractRuntimeDataFetcher<T> {

    GraphQLDeleteResponseHandler responseHandler

    DeletingRuntimeDataFetcher(PersistentEntity entity,
                               GraphQLDataFetcherManager fetcherManager,
                               GraphQLInterceptorManager interceptorManager,
                               GraphQLDeleteResponseHandler responseHandler) {
        super(entity, fetcherManager, interceptorManager, GraphQLDataFetcherType.DELETE)
        this.responseHandler = responseHandler
    }

    @Override
    DataFetcher resolveFetcher() {
        fetcherManager.getDeletingFetcher(entity, responseHandler)
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
