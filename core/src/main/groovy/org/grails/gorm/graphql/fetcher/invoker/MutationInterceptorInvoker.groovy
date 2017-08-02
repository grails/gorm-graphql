package org.grails.gorm.graphql.fetcher.invoker

import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor

/**
 * An interceptor invoker that will call the
 * {@link GraphQLFetcherInterceptor#onCustomMutation} method
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class MutationInterceptorInvoker implements InterceptorInvoker {

    @Override
    boolean invoke(GraphQLFetcherInterceptor interceptor, String name, DataFetchingEnvironment environment) {
        interceptor.onCustomMutation(name, environment)
    }
}
