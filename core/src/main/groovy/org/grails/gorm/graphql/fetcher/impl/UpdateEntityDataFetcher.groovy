package org.grails.gorm.graphql.fetcher.impl

import grails.gorm.transactions.Transactional
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.datastore.gorm.GormEntity
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.fetcher.BindingGormDataFetcher
import org.grails.gorm.graphql.fetcher.DefaultGormDataFetcher
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType

/**
 * A class for updating an entity with GraphQL
 *
 * @param <T> The domain returnType to update
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
@InheritConstructors
class UpdateEntityDataFetcher<T> extends DefaultGormDataFetcher<T> implements BindingGormDataFetcher {

    GraphQLDataBinder dataBinder

    @Override
    @Transactional
    T get(DataFetchingEnvironment environment) {
        GormEntity instance = getInstance(environment)
        dataBinder.bind(instance, getArgument(environment))
        if (!instance.hasErrors()) {
            instance.save()
        }
        (T)instance
    }

    protected GormEntity getInstance(DataFetchingEnvironment environment) {
        queryInstance(environment)
    }

    protected Map getArgument(DataFetchingEnvironment environment) {
        (Map)environment.getArgument(entity.decapitalizedName)
    }

    @Override
    boolean supports(GraphQLDataFetcherType type) {
        type == GraphQLDataFetcherType.UPDATE
    }
}
