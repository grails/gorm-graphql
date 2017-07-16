package org.grails.gorm.graphql.fetcher

import graphql.schema.DataFetcher
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler

/**
 * An interface to describe data fetchers that delete
 *
 * @author James Kleeh
 * @since 1.0.0
 */
interface DeletingGormDataFetcher extends DataFetcher {

    void setResponseHandler(GraphQLDeleteResponseHandler responseHandler)
}
