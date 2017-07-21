package grails.test.app

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class GraphqlControllerSpec extends Specification implements GraphQLSpec {

    def "calling graphql endpoint without supplying a query should not crash"() {
        when:
        def response = rest.get(url)

        then:
        response.statusCode.value() == 422
    }
}