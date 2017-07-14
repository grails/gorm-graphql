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
                title
                dateCreated
                lastUpdated
                tags {
                  id
                  name
                }
              }
            }
        """)
        def obj = resp.json.data.postCreate


        then:
        obj.id
        obj.title == 'Grails 3.3 Release'
        obj.tags.size() == 3
        obj.tags.find { it.name == 'Grails' }
        obj.tags.find { it.name == 'Groovy' }
        obj.tags.find { it.name == 'Java' }
        obj.dateCreated != null
        obj.lastUpdated != null
    }
}
