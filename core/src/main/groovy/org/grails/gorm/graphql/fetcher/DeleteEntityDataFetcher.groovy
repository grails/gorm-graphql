package org.grails.gorm.graphql.fetcher

import grails.gorm.transactions.Transactional
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.datastore.gorm.GormEntity

@CompileStatic
@InheritConstructors
class DeleteEntityDataFetcher<T> extends GormDataFetcher<T> {

    @Override
    @Transactional
    T get(DataFetchingEnvironment environment) {
        GormEntity instance = queryInstance(environment)

        Map response = [success: false]
        try {
            ((GormEntity)instance).delete(failOnError: true)
            response.success = true
        } catch (e) {}

        (T)response
    }

}