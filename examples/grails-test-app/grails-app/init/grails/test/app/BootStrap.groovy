package grails.test.app

class BootStrap {

    def init = { servletContext ->

        new Author(name: "Sally")
            .addToBooks(title: "Book 1")
            .addToBooks(title: "Book 2")
            .addToBooks(title: "Book 3")
            .save(flush: true, failOnError: true)

    }
    def destroy = {
    }
}
