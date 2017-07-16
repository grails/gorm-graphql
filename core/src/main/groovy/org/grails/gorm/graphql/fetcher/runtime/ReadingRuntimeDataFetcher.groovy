package org.grails.gorm.graphql.fetcher.runtime

import graphql.schema.DataFetcher
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager

/**
 * A runtime data fetcher implementation used for fetchers
 * that read data
 *
 * @see AbstractRuntimeDataFetcher
 *
 * @param <T> The domain type to return
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class ReadingRuntimeDataFetcher<T> extends AbstractRuntimeDataFetcher<T> {

    GraphQLDataFetcherType type
    PersistentEntity entity

    ReadingRuntimeDataFetcher(PersistentEntity entity,
                              GraphQLDataFetcherManager manager,
                              GraphQLDataFetcherType type) {
        super(manager)
        this.type = type
        this.entity = entity
    }

    @Override
    DataFetcher resolveFetcher() {
        manager.getReadingFetcher(entity, type)
    }
}
