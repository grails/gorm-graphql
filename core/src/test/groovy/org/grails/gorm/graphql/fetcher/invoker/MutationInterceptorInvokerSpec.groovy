package org.grails.gorm.graphql.fetcher.invoker

import graphql.schema.DataFetchingEnvironment
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor
import spock.lang.Specification

class MutationInterceptorInvokerSpec extends Specification {

    void "test invoke"() {
        GraphQLFetcherInterceptor interceptor = Mock(GraphQLFetcherInterceptor)
        DataFetchingEnvironment environment = Mock(DataFetchingEnvironment)

        when:
        new MutationInterceptorInvoker().invoke(interceptor, 'foo', environment)

        then:
        1 * interceptor.onCustomMutation('foo', environment)
    }
}
