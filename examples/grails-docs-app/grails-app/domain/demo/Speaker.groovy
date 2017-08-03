// tag::wholeFile[]
package demo

class Speaker {

    String firstName
    String lastName
    String name
    String email
    String bio

    static hasMany = [talks: Talk]

    static graphql = true // <1>

    static constraints = {
        email nullable: true, email: true
        bio nullable: true
    }

    static mapping = {
        name formula: 'concat(FIRST_NAME,\' \',LAST_NAME)'
    }
}
// end::wholeFile[]