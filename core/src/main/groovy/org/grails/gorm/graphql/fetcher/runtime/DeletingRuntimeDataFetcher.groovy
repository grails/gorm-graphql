package org.grails.gorm.graphql.fetcher.runtime

import graphql.schema.DataFetcher
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler

/**
 * A runtime data fetcher implementation used for fetchers
 * that delete
 *
 * @see AbstractRuntimeDataFetcher
 *
 * @param <T> The domain type to return
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DeletingRuntimeDataFetcher<T> extends AbstractRuntimeDataFetcher<T> {

    GraphQLDeleteResponseHandler responseHandler

    DeletingRuntimeDataFetcher(PersistentEntity entity,
                               GraphQLDataFetcherManager manager,
                               GraphQLDeleteResponseHandler responseHandler) {
        super(entity, manager, GraphQLDataFetcherType.DELETE)
        this.responseHandler = responseHandler
    }

    @Override
    DataFetcher resolveFetcher() {
        manager.getDeletingFetcher(entity, responseHandler)
    }
}
