package grails.test.app

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class BookIntegrationSpec extends Specification implements GraphQLSpec {

    void "test books cannot be queried directly"() {
        when:
        def resp = graphQL.graphql("""
            {
              bookList {
                id
              }
            }
        """)

        def result = resp.json

        then:
        result.errors.size() == 1
        result.errors[0].message == "Validation error of returnType FieldUndefined: Field bookList is undefined"
    }

}
