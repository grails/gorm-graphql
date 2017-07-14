package org.grails.gorm.graphql.fetcher.manager.runtime

import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager

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
    T get(DataFetchingEnvironment environment) {
        return (T)manager.getReadingFetcher(entity, type).get(environment)
    }
}
