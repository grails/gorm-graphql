package org.grails.gorm.graphql.fetcher.impl

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor
import org.grails.gorm.graphql.interceptor.manager.GraphQLInterceptorManager

/**
 * Used to wrap data fetchers provided by custom operations to apply
 * interceptor behavior.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Slf4j
@CompileStatic
class CustomOperationInterceptorDataFetcher implements DataFetcher {

    protected Class clazz
    protected DataFetcher wrappedFetcher
    protected GraphQLInterceptorManager interceptorManager

    private List<GraphQLFetcherInterceptor> interceptors

    CustomOperationInterceptorDataFetcher(Class clazz,
                                          DataFetcher wrappedFetcher,
                                          GraphQLInterceptorManager interceptorManager) {
        this.clazz = clazz
        this.wrappedFetcher = wrappedFetcher
        this.interceptorManager = interceptorManager
    }

    @Override
    Object get(DataFetchingEnvironment environment) {
        if (interceptors == null) {
            interceptors = interceptorManager.getInterceptors(clazz) ?: (List<GraphQLFetcherInterceptor>)[]
        }

        final String NAME = environment.fields.empty ? 'UNKNOWN' : environment.fields[0].name

        for (GraphQLFetcherInterceptor i: interceptors) {
            if (!i.onCustomOperation(NAME, environment)) {
                log.info("Execution of ${NAME} was prevented by an interceptor")
                return null
            }
        }
        wrappedFetcher.get(environment)
    }
}
