package org.grails.gorm.graphql.fetcher

import grails.gorm.transactions.Transactional
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.binding.GraphQLDataBinder

/**
 * A class for updating an entity with GraphQL
 *
 * @param <T> The domain type to update
 * @author James Kleeh
 */
@CompileStatic
class UpdateEntityDataFetcher<T> extends GormDataFetcher<T> {

    protected GraphQLDataBinder dataBinder

    UpdateEntityDataFetcher(PersistentEntity entity, GraphQLDataBinder dataBinder) {
        super(entity)
        this.dataBinder = dataBinder
    }

    @Override
    @Transactional
    T get(DataFetchingEnvironment environment) {
        GormEntity instance = queryInstance(environment)
        dataBinder.bind(instance, (Map)environment.getArgument(entity.decapitalizedName))
        instance.save()
        (T)instance
    }

}
