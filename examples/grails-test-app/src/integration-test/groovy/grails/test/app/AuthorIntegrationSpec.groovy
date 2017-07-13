package grails.test.app

import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import spock.lang.Stepwise

@Integration
@Stepwise
class AuthorIntegrationSpec extends Specification implements GraphQLSpec {

    void "test creating an author with multiple books"() {
        when:
        def resp = post("""
            mutation {
              authorCreate(author: {
                name: "Sally",
                books: [
                  {title: "Book 1"},
                  {title: "Book 2"},
                  {title: "Book 3"}
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

        def obj = resp.json.result.data.authorCreate

        then:
        obj.id == 1
        obj.name == "Sally"
        obj.books.size() == 3
        obj.books.find { it.id == 1 }.title == "Book 1"
        obj.books.find { it.id == 2 }.title == "Book 2"
        obj.books.find { it.id == 3 }.title == "Book 3"
    }

    void "test creating an author with a book"() {
        when:
        def resp = post("""
            mutation {
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
            }
        """)


        def obj = resp.json.result.data.authorCreate

        then:
        obj.id == 2
        obj.name == "Joseph"
        obj.books.size() == 1
        obj.books[0].id == 4
        obj.books[0].title == "XYZ"
    }

    void "test listing authors"() {
        when:
        def resp = post("""
            {
              authorList {
                id
                name
                books {
                  id
                  title
                }
              }
            }
        """)

        def authors = resp.json.result.data.authorList
        def author1 = authors[0]
        def author2 = authors[1]

        then:
        authors.size() == 2
        author1.id == 1
        author1.name == "Sally"
        author1.books.size() == 3
        author1.books.find { it.id == 1 }.title == "Book 1"
        author1.books.find { it.id == 2 }.title == "Book 2"
        author1.books.find { it.id == 3 }.title == "Book 3"
        author2.id == 2
        author2.name == "Joseph"
        author2.books.size() == 1
        author2.books[0].id == 4
        author2.books[0].title == "XYZ"
    }

    void "test querying a single author"() {
        when:
        def resp = post("""
            {
              author(id: 2) {
                id
                name
                books {
                  id
                  title
                }
              }
            }
        """)

        def author = resp.json.result.data.author

        then:
        author.id == 2
        author.name == "Joseph"
        author.books.size() == 1
        author.books[0].id == 4
        author.books[0].title == "XYZ"
    }

    void "test updating a book on an author"() {
        when:
        def resp = post("""
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

        def obj = resp.json.result.data.authorUpdate

        then:
        obj.id == 1
        obj.name == "Xavier"
        obj.books.size() == 2
        obj.books.find { it.id == 1 }.title == "x"
        obj.books.find { it.id == 2 }.title == "Book 2"
    }

    void "test deleting an author"() {
        when:
        def resp = post("""
            mutation {
              authorDelete(id: 2) {
                success
              }
            }
        """)

        then:
        resp.json.result.data.authorDelete.success == true
    }

    void cleanupSpec() {
        post("""
            mutation {
              authorDelete(id: 1) {
                success
              }
            }
        """)
    }

}
