package grails.test.app

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class PostIntegrationSpec extends Specification implements GraphQLSpec {

    void "test creating a post with tags"() {
        when:
        def resp = post("""
            mutation {
              postCreate(post: {
                title: "Grails 3.3 Release",
                tags: [
                  {name: "Grails"}
                  {name: "Groovy"}
                  {name: "Java"}
                ]
              }) {
                id
                name
                books {
                  id
                  title
                }
              }
            }
        """)
    }
}
