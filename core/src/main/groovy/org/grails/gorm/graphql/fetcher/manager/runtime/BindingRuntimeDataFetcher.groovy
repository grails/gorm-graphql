package org.grails.gorm.graphql.fetcher.manager.runtime

import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager

@CompileStatic
class BindingRuntimeDataFetcher<T> extends AbstractRuntimeDataFetcher<T> {

    GraphQLDataBinder dataBinder
    GraphQLDataFetcherType type
    PersistentEntity entity

    BindingRuntimeDataFetcher(PersistentEntity entity,
                              GraphQLDataFetcherManager manager,
                              GraphQLDataBinder dataBinder,
                              GraphQLDataFetcherType type) {
        super(manager)
        this.dataBinder = dataBinder
        this.type = type
        this.entity = entity
    }

    @Override
    T get(DataFetchingEnvironment environment) {
        return (T)manager.getBindingFetcher(entity, dataBinder, type).get(environment)
    }
}
