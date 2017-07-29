package org.grails.gorm.graphql.fetcher.runtime

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.fetcher.DataFetcherNotFoundException
import org.grails.gorm.graphql.fetcher.GormDataFetcher
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor
import org.grails.gorm.graphql.interceptor.manager.GraphQLInterceptorManager

/**
 * A base class to extend from to resolve data fetchers at runtime
 * instead of during schema generation. This allows new fetchers, data binders,
 * etc to be registered after the schema is created, but before it is used.
 *
 * @param <T> The domain returnType to return
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
@Slf4j
abstract class AbstractRuntimeDataFetcher<T> implements DataFetcher<T>, GormDataFetcher {

    GraphQLDataFetcherManager fetcherManager
    GraphQLInterceptorManager interceptorManager
    PersistentEntity entity
    GraphQLDataFetcherType type

    private DataFetcher resolvedFetcher
    protected List<GraphQLFetcherInterceptor> interceptors

    AbstractRuntimeDataFetcher(PersistentEntity entity,
                               GraphQLDataFetcherManager fetcherManager,
                               GraphQLInterceptorManager interceptorManager,
                               GraphQLDataFetcherType type) {
        this.entity = entity
        this.fetcherManager = fetcherManager
        this.interceptorManager = interceptorManager
        this.type = type
    }

    @Override
    boolean supports(GraphQLDataFetcherType type) {
        this.type == type
    }

    @Override
    T get(DataFetchingEnvironment environment) {
        if (interceptors == null) {
            interceptors = interceptorManager.getInterceptors(entity.javaClass) ?: (List<GraphQLFetcherInterceptor>)[]
        }
        if (!intercept(environment)) {
            final String NAME = environment.fields.empty ? 'UNKNOWN' : environment.fields[0].name
            log.info("Execution of ${NAME} was prevented by an interceptor")
            return null
        }
        if (resolvedFetcher == null) {
            resolvedFetcher = resolveFetcher()
        }
        if (resolvedFetcher == null) {
            throw new DataFetcherNotFoundException(entity, type)
        }
        resolvedFetcher.get(environment)
    }

    /**
     * @return The data fetcher to be used. The result will be cached.
     */
    abstract DataFetcher resolveFetcher()

    abstract boolean intercept(DataFetchingEnvironment environment)
}
