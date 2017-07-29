package org.grails.gorm.graphql.fetcher.invoker

import graphql.schema.DataFetchingEnvironment
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor

/**
 * A generic interface for custom operations to separate which event
 * will be called based on the returnType of the operation.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
interface InterceptorInvoker {

    boolean invoke(GraphQLFetcherInterceptor interceptor, String name, DataFetchingEnvironment environment)
}
