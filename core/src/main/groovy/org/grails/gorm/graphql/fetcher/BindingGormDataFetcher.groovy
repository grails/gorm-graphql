package org.grails.gorm.graphql.fetcher

import graphql.schema.DataFetcher
import org.grails.gorm.graphql.binding.GraphQLDataBinder

/**
 * An interface to describe data fetchers that use data binding
 *
 * @author James Kleeh
 * @since 1.0.0
 */
interface BindingGormDataFetcher extends DataFetcher {

    void setDataBinder(GraphQLDataBinder dataBinder)

    GraphQLDataFetcherType getType()
}
