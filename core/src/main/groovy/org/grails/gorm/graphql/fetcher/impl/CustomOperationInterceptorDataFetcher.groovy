package org.grails.gorm.graphql.fetcher.impl

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.gorm.graphql.entity.operations.OperationType
import org.grails.gorm.graphql.fetcher.invoker.InterceptorInvoker
import org.grails.gorm.graphql.fetcher.invoker.MutationInterceptorInvoker
import org.grails.gorm.graphql.fetcher.invoker.QueryInterceptorInvoker
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
    protected InterceptorInvoker interceptorInvoker

    private List<GraphQLFetcherInterceptor> interceptors
    private static InterceptorInvoker queryInterceptorInvoker = new QueryInterceptorInvoker()
    private static InterceptorInvoker mutationInterceptorInvoker = new MutationInterceptorInvoker()

    CustomOperationInterceptorDataFetcher(Class clazz,
                                          DataFetcher wrappedFetcher,
                                          GraphQLInterceptorManager interceptorManager,
                                          OperationType operationType) {
        this.clazz = clazz
        this.wrappedFetcher = wrappedFetcher
        this.interceptorManager = interceptorManager
        if (operationType == OperationType.QUERY) {
            this.interceptorInvoker = queryInterceptorInvoker
        }
        else if (operationType == OperationType.MUTATION) {
            this.interceptorInvoker = mutationInterceptorInvoker
        }
    }

    @Override
    Object get(DataFetchingEnvironment environment) {
        if (interceptors == null) {
            interceptors = interceptorManager.getInterceptors(clazz) ?: (List<GraphQLFetcherInterceptor>)[]
        }

        final String NAME = environment.fields.empty ? 'UNKNOWN' : environment.fields[0].name

        for (GraphQLFetcherInterceptor i: interceptors) {
            if (!interceptorInvoker.invoke(i, NAME, environment)) {
                log.info("Execution of ${NAME} was prevented by an interceptor")
                return null
            }
        }

        wrappedFetcher.get(environment)
    }

}
