package org.grails.gorm.graphql.fetcher.manager.runtime

import graphql.schema.DataFetcher
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager

/**
 * Created by jameskleeh on 7/14/17.
 */
abstract class AbstractRuntimeDataFetcher<T> implements DataFetcher<T> {

    GraphQLDataFetcherManager manager

    AbstractRuntimeDataFetcher(GraphQLDataFetcherManager manager) {
        this.manager = manager
    }

}
