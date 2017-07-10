package org.grails.gorm.graphql.fetcher

import grails.gorm.transactions.Transactional
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * A class for querying a single entity with GraphQL
 *
 * @param <T> The domain type to query
 * @author James Kleeh
 */
@CompileStatic
@InheritConstructors
class SingleEntityDataFetcher<T> extends GormDataFetcher<T> {

    @Override
    @Transactional(readOnly = true)
    T get(DataFetchingEnvironment environment) {
        (T)queryInstance(environment)
    }
}
