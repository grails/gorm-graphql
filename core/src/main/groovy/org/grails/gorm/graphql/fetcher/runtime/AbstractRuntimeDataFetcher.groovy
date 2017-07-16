package org.grails.gorm.graphql.fetcher.runtime

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager

/**
 * A base class to extend from to resolve data fetchers at runtime
 * instead of during schema generation. This allows new fetchers, data binders,
 * etc to be registered after the schema is created, but before it is used.
 *
 * @param <T> The domain type to return
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
abstract class AbstractRuntimeDataFetcher<T> implements DataFetcher<T> {

    GraphQLDataFetcherManager manager

    private DataFetcher resolvedFetcher

    AbstractRuntimeDataFetcher(GraphQLDataFetcherManager manager) {
        this.manager = manager
    }

    T get(DataFetchingEnvironment environment) {
        if (resolvedFetcher == null) {
            resolvedFetcher = resolveFetcher()
        }
        resolvedFetcher.get(environment)
    }

    /**
     * @return The data fetcher to be used. The result will be cached.
     */
    abstract DataFetcher resolveFetcher()
}
