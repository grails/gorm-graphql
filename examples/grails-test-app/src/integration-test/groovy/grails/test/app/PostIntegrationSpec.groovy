package grails.test.app

import org.grails.gorm.graphql.plugin.testing.GraphQLSpec
import grails.testing.mixin.integration.Integration
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.text.SimpleDateFormat

@Integration
@Stepwise
class PostIntegrationSpec extends Specification implements GraphQLSpec {

    @Shared SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")

    void "test creating a post without tags"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
              postCreate(post: {
                title: "Temporary Post"
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
        obj.title == 'Temporary Post'
        obj.tags == null
        obj.dateCreated != null
        obj.lastUpdated != null

        cleanup:
        graphQL.graphql("""
            mutation {
              postDelete(id: $obj.id) {
                success
              }
            }
        """)
    }

    void "test creating a post with tags"() {
        when:
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
        obj.id == 2
        obj.title == 'Grails 3.3 Release'
        obj.tags.size() == 3
        obj.tags.find { it.name == 'Grails' }
        obj.tags.find { it.name == 'Groovy' }
        obj.tags.find { it.name == 'Java' }
        obj.dateCreated != null
        obj.lastUpdated != null
    }

    void "test creating a post with an existing tag"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
              postCreate(post: {
                title: "Grails 3.4 Release",
                tags: [
                  {id: 1}
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
        obj.id == 3
        obj.title == 'Grails 3.4 Release'
        obj.tags.size() == 1
        obj.tags.find { it.id == 1 }
        obj.dateCreated != null
        obj.lastUpdated != null
    }

    void "test updating a post"() {
        when:
        Thread.sleep(1000)
        def resp = graphQL.graphql("""
            mutation {
              postUpdate(id: 3, post: {
                title: "Grails 3.5 Release",
                tags: [
                    {id: 1},
                    {id: 2}
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
        def obj = resp.json.data.postUpdate

        then:
        obj.id
        obj.title == 'Grails 3.5 Release'
        obj.tags.size() == 2
        obj.tags.find { it.id == 1 }
        obj.tags.find { it.id == 2 }
        format.parse(obj.lastUpdated) > format.parse(obj.dateCreated)
    }

    void "test listing posts"() {
        when:
        def resp = graphQL.graphql("""
            {
              postList(sort: "id") {
                title
                tags {
                  id
                  name
                }
              }
            }
        """)
        def obj = resp.json.data.postList

        then:
        obj.size() == 2
        obj[0].title == 'Grails 3.3 Release'
        obj[0].tags.size() == 3
        obj[1].title == 'Grails 3.5 Release'
        obj[1].tags.size() == 2
    }

    void "test paginating posts"() {
        when:
        def resp = graphQL.graphql("""
            {
              postList(sort: "id", max: 1) {
                title
              }
            }
        """)
        def obj = resp.json.data.postList

        then:
        obj.size() == 1
        obj[0].title == 'Grails 3.3 Release'

        when:
        resp = graphQL.graphql("""
            {
              postList(sort: "id", max: 1, offset: 1) {
                title
              }
            }
        """)
        obj = resp.json.data.postList

        then:
        obj.size() == 1
        obj[0].title == 'Grails 3.5 Release'
    }

    void "test query a single post"() {
        when:
        def resp = graphQL.graphql("""
            {
              post(id: 3) {
                title
              }
            }
        """)
        def obj = resp.json.data.post

        then:
        obj.title == 'Grails 3.5 Release'
    }

    void "test deleting a post"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
              postDelete(id: 3) {
                success
              }
            }
        """)
        def obj = resp.json.data.postDelete

        then:
        obj.success
    }

    void cleanupSpec() {
        graphQL.graphql("""
            mutation {
              postDelete(id: 2) {
                success
              }
            }
        """)
        def resp = graphQL.graphql("""
            { 
              tagList {
                id
              }
            }
        """)
        def tags = resp.json.data.tagList
        tags.each {
            resp = graphQL.graphql("""
              mutation {
                tagDelete(id: ${it.id}) {
                  success
                }
              }
            """)
        }
    }
}
