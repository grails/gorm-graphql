package grails.test.app

import org.grails.gorm.graphql.plugin.testing.GraphQLSpec
import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import org.grails.web.json.JSONArray
import spock.lang.Specification

@Integration
class TagIntegrationSpec extends Specification implements GraphQLSpec {

    @OnceBefore
    void createPosts() {
        def resp = graphQL.graphql("""
            mutation {
              postCreate(post: {
                title: "Grails 3.3 Release",
                tags: [
                  {name: "Grails"},
                  {name: "Groovy"},
                  {name: "Java"}
                ]
              }) {
                id
                tags {
                  id
                  name
                }
              }
            }
        """)
        JSONArray obj = resp.json.data.postCreate.tags
        def grails = obj.find { it.name == 'Grails' }.id
        def groovy = obj.find { it.name == 'Groovy' }.id

        resp = graphQL.graphql("""
            mutation {
              postCreate(post: {
                title: "Grails 3.4 Release",
                tags: [
                  {id: ${grails}},
                  {id: ${groovy}},
                  {name: "Kotlin Ken"}
                ]
              }) {
                id
                tags {
                  id 
                  name
                }
              }
            }
        """)
        assert resp.json.data.postCreate.tags.size() == 3
    }

    void "test a custom property can reference a domain"() {
        when:
        def resp = graphQL.graphql("""
            {
              tagList(sort: "id") {
                id
                name
                posts {
                  id
                }
              }
            }
        """)
        JSONArray obj = resp.json.data.tagList

        then:
        obj.size() == 4
        obj.find { it.name == 'Grails' }.posts.size() == 2
        obj.find { it.name == 'Groovy' }.posts.size() == 2
        obj.find { it.name == 'Java' }.posts.size() == 1
        obj.find { it.name == 'Kotlin Ken' }.posts.size() == 1
    }
}
