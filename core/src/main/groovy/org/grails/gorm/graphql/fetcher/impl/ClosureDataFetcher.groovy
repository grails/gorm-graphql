package org.grails.gorm.graphql.fetcher.impl

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic

/**
 * A class to retrieve data from the environment source
 * with a closure.
 *
 * @author James Kleeh
 */
@CompileStatic
class ClosureDataFetcher implements DataFetcher<Object> {

    private Closure closure

    ClosureDataFetcher(Closure closure) {
        this.closure = closure
    }

    @Override
    Object get(DataFetchingEnvironment environment) {
        Object source = environment.getSource()
        closure.call(source)
    }
}
