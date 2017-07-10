package org.grails.gorm.graphql.fetcher.manager

import graphql.schema.DataFetcher
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler

/**
 * An interface to create data fetcher instances
 *
 * @author James Kleeh
 */
interface GraphQLDataFetcherManager {

    DataFetcher createGetFetcher(PersistentEntity entity)

    DataFetcher createListFetcher(PersistentEntity entity)

    DataFetcher createCreateFetcher(PersistentEntity entity, GraphQLDataBinder dataBinder)

    DataFetcher createUpdateFetcher(PersistentEntity entity, GraphQLDataBinder dataBinder)

    DataFetcher createDeleteFetcher(PersistentEntity entity, GraphQLDeleteResponseHandler responseHandler)
}
