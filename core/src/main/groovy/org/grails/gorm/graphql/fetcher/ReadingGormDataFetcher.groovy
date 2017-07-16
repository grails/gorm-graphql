package org.grails.gorm.graphql.fetcher

import graphql.schema.DataFetcher

/**
 * An interface to describe data fetchers that read
 *
 * @author James Kleeh
 * @since 1.0.0
 */
interface ReadingGormDataFetcher extends DataFetcher {

    GraphQLDataFetcherType getType()
}
