package grails.test.app

class Author {

    String name

    static hasMany = [books: Book]

    static constraints = {
        name maxSize: 20
    }

    static graphql = true

    /*
    * static graphql = {
    *   name input: false
    *   password output: false, name: 'xyz'
    *   add('number1', Integer) {
    *       output false
    *   }
    *   add('number2', Integer) {
    *       dataFetcher { Author author ->
    *           author.foo
    *       }
    *   }
    *
    * }
    * */
}
