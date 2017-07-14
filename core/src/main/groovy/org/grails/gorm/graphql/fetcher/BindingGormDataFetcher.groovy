package org.grails.gorm.graphql.fetcher

import graphql.schema.DataFetcher
import org.grails.gorm.graphql.binding.GraphQLDataBinder

/**
 * Created by jameskleeh on 7/14/17.
 */
interface BindingGormDataFetcher extends DataFetcher {

    void setDataBinder(GraphQLDataBinder dataBinder)

    GraphQLDataFetcherType getType()
}