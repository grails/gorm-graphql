package org.grails.gorm.graphql.fetcher.runtime

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor

/**
 * A runtime data fetcher implementation used for fetchers
 * that read data
 *
 * @see AbstractRuntimeDataFetcher
 *
 * @param <T> The domain returnType to return
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
@InheritConstructors
class ReadingRuntimeDataFetcher<T> extends AbstractRuntimeDataFetcher<T> {

    @Override
    DataFetcher resolveFetcher() {
        fetcherManager.getReadingFetcher(entity, type)
    }

    boolean intercept(DataFetchingEnvironment environment) {
        for (GraphQLFetcherInterceptor i: interceptors) {
            if (!i.onQuery(environment, type)) {
                return false
            }
        }
        true
    }
}
