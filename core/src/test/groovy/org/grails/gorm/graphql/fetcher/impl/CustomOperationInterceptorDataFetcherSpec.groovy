package org.grails.gorm.graphql.fetcher.impl

import graphql.language.Field
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.grails.gorm.graphql.entity.operations.OperationType
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor
import org.grails.gorm.graphql.interceptor.manager.GraphQLInterceptorManager
import spock.lang.Specification

class CustomOperationInterceptorDataFetcherSpec extends Specification {

    private DataFetchingEnvironment buildMockEnvironment() {
        Stub(DataFetchingEnvironment) {
            getFields() >> [Stub(Field) {
                getName() >> 'foo'
            }]
        }
    }

    void "test interceptors are invoked for QUERY"() {
        given:
        DataFetchingEnvironment environment = buildMockEnvironment()
        GraphQLInterceptorManager interceptorManager = Mock(GraphQLInterceptorManager) {
            1 * getInterceptors(String) >> [Mock(GraphQLFetcherInterceptor) {
                1 * onCustomQuery('foo', environment) >> true
            }]
        }
        DataFetcher wrappedFetcher = Mock(DataFetcher)

        when:
        def fetcher = new CustomOperationInterceptorDataFetcher(String, wrappedFetcher, interceptorManager, OperationType.QUERY)
        fetcher.get(environment)

        then:
        1 * wrappedFetcher.get(environment)
    }

    void "test interceptors are invoked for MUTATION"() {
        given:
        DataFetchingEnvironment environment = buildMockEnvironment()
        GraphQLInterceptorManager interceptorManager = Mock(GraphQLInterceptorManager) {
            1 * getInterceptors(String) >> [Mock(GraphQLFetcherInterceptor) {
                1 * onCustomMutation('foo', environment) >> true
            }]
        }
        DataFetcher wrappedFetcher = Mock(DataFetcher)

        when:
        def fetcher = new CustomOperationInterceptorDataFetcher(String, wrappedFetcher, interceptorManager, OperationType.MUTATION)
        fetcher.get(environment)

        then:
        1 * wrappedFetcher.get(environment)
    }


    void "test execution is prevented"() {
        given:
        DataFetchingEnvironment environment = buildMockEnvironment()
        GraphQLInterceptorManager interceptorManager = Mock(GraphQLInterceptorManager) {
            1 * getInterceptors(String) >> [Mock(GraphQLFetcherInterceptor) {
                1 * onCustomMutation('foo', environment) >> true
            },Mock(GraphQLFetcherInterceptor) {
                1 * onCustomMutation('foo', environment) >> false
            },Mock(GraphQLFetcherInterceptor) {
                0 * onCustomMutation('foo', environment) >> true
            }]
        }
        DataFetcher wrappedFetcher = Mock(DataFetcher)

        when:
        def fetcher = new CustomOperationInterceptorDataFetcher(String, wrappedFetcher, interceptorManager, OperationType.MUTATION)
        def result = fetcher.get(environment)

        then:
        0 * wrappedFetcher.get(environment)
        result == null
    }

}
