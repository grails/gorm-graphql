package grails.test.app

import grails.plugins.rest.client.RestBuilder
import grails.testing.mixin.integration.Integration
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@Integration
@Stepwise
class AuthorIntegrationSpec extends Specification {

    @Shared RestBuilder rest
    @Shared String url

    void setupSpec() {
        rest = new RestBuilder()
    }

    void setup() {
        url = "http://localhost:${serverPort}/graphql"
    }

    void "test creating an author with a book"() {
        when:
        def resp = rest.post(url) {
            contentType('application/graphql')
            body("""mutation {
                  authorCreate(author: {
                    name: "Joseph",
                    books: [
                      {title: "XYZ"}
                    ]
                  }) {
                    id
                    name
                    books {
                      id
                      title
                    }
                  }
                } """)
        }
        def obj = resp.json.result.data.authorCreate

        then:
        obj.id == 2
        obj.name == "Joseph"
        obj.books.size() == 1
        obj.books[0].id == 4
        obj.books[0].title == "XYZ"
    }

    void "test updating a book on an author"() {
        when:
        def resp = rest.post(url) {
            contentType('application/graphql')
            body("""
                mutation {
                  authorUpdate(id: 1, author: {
                    name: "Xavier",
                    books: [
                      {id: 1, title: "x"},
                      {id: 2}
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
        def obj = resp.json.result.data.authorUpdate

        then:
        obj.id == 1
        obj.name == "Xavier"
        obj.books.size() == 2
        obj.books.find { it.id == 1 }.title == "x"
        obj.books.find { it.id == 2 }.title == "Book 2"


    }

}
