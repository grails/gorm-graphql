package org.grails.gorm.graphql.interceptor.impl

import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor

import java.lang.reflect.ParameterizedType

/**
 * Base class to extend from for custom data fetcher interceptors. Provides default
 * implementations of all methods.
 *
 * @param <T> The class to intercept
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
abstract class AbstractGraphQLFetcherInterceptor<T> implements GraphQLFetcherInterceptor {

    private Class resolvedType

    Class<T> getSupportedType() {
        if (resolvedType == null) {
            ParameterizedType parameterizedType = (ParameterizedType)getClass().genericInterfaces.find { genericInterface ->
                genericInterface instanceof ParameterizedType &&
                        AbstractGraphQLFetcherInterceptor.isAssignableFrom((Class)((ParameterizedType)genericInterface).rawType)
            }

            if (parameterizedType?.actualTypeArguments != null) {
                resolvedType = (Class<T>)parameterizedType.actualTypeArguments[0]
            } else {
                resolvedType = Object
            }
        }
        resolvedType
    }

    boolean onQuery(DataFetchingEnvironment environment, GraphQLDataFetcherType type) {
        true
    }

    boolean onMutation(DataFetchingEnvironment environment, GraphQLDataFetcherType type) {
        true
    }

    boolean onCustomOperation(String name, DataFetchingEnvironment environment) {
        true
    }

}
