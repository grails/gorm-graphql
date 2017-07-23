package org.grails.gorm.graphql.interceptor.manager

import groovy.transform.CompileStatic
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor
import org.grails.gorm.graphql.types.KeyClassQuery

/**
 * Default implementation of {@link GraphQLInterceptorManager} that
 * will also return a result if the class requested is a subclass
 * of a class that exists in the registry. The order of which interceptors
 * are registered is relevant to their resolution. The items added last
 * have priority when searching for subclass matches.
 *
 * Example:
 * register(Collection)
 * register(List)
 *
 * When the binder is searched for ArrayList, List will be returned.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DefaultGraphQLInterceptorManager implements GraphQLInterceptorManager, KeyClassQuery<List<GraphQLFetcherInterceptor>> {

    protected Map<Class, List<GraphQLFetcherInterceptor>> interceptors = Collections.synchronizedMap([:]).withDefault { [] }

    /**
     * @see GraphQLInterceptorManager#registerInterceptor
     */
    @Override
    void registerInterceptor(GraphQLFetcherInterceptor interceptor) {
        interceptors.get(interceptor.supportedType).add(interceptor)
    }

    /**
     * @see GraphQLInterceptorManager#getInterceptors
     *
     * @return NULL if no interceptors found
     */
    @Override
    List<GraphQLFetcherInterceptor> getInterceptors(Class clazz) {
        searchMap(interceptors, clazz)
    }
}
