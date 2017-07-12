package grails32.test.app

class Author {

    String name

    static hasMany = [books: Book]

    static constraints = {
    }

    static graphql = true
}
