package grails.test.app

import grails.test.app.inheritance.*

class BootStrap {

    def init = { servletContext ->
        Map args = [flush: true, failOnError: true]
        new Dog(name: "Spot", moveSpeed: 60).save(args)
        new Labradoodle(name: "Chloe", moveSpeed: 60).save(args)
        new Human(name: "Kotlin Ken").save(args)
    }
    def destroy = {
    }
}
