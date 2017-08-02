package org.grails.gorm.graphql.fetcher.invoker

import graphql.schema.DataFetchingEnvironment
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor
import spock.lang.Specification

class QueryInterceptorInvokerSpec extends Specification {

    void "test invoke"() {
        GraphQLFetcherInterceptor interceptor = Mock(GraphQLFetcherInterceptor)
        DataFetchingEnvironment environment = Mock(DataFetchingEnvironment)

        when:
        new QueryInterceptorInvoker().invoke(interceptor, 'foo', environment)

        then:
        1 * interceptor.onCustomQuery('foo', environment)
    }
}
