package org.grails.gorm.graphql.fetcher

import graphql.schema.DataFetcher

/**
 * Created by jameskleeh on 7/14/17.
 */
interface ReadingGormDataFetcher extends DataFetcher {

    GraphQLDataFetcherType getType()
}