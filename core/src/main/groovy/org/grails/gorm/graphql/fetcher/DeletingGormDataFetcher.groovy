package org.grails.gorm.graphql.fetcher

import graphql.schema.DataFetcher
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler

interface DeletingGormDataFetcher extends DataFetcher {

    void setResponseHandler(GraphQLDeleteResponseHandler responseHandler)
}