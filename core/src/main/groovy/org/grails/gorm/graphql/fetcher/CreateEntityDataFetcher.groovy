package org.grails.gorm.graphql.fetcher

import grails.gorm.transactions.Transactional
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.binding.GraphQLDataBinder

@CompileStatic
class CreateEntityDataFetcher<T> extends GormDataFetcher<T> {

    GraphQLDataBinder dataBinder

    CreateEntityDataFetcher(PersistentEntity entity, GraphQLDataBinder dataBinder) {
        super(entity)
        this.dataBinder = dataBinder
    }

    @Override
    @Transactional
    T get(DataFetchingEnvironment environment) {
        GormEntity instance = (GormEntity)entity.javaClass.newInstance()
        dataBinder.bind(instance, (Map)environment.getArgument(entity.decapitalizedName))
        instance.save()
        (T)instance
    }

}
