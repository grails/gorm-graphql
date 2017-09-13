package grails.test.app

import grails.test.app.inheritance.*

class BootStrap {

    def init = { servletContext ->
        Map args = [flush: true, failOnError: true]
        new Dog(name: "Spot", moveSpeed: 60).save(args)
        new Labradoodle(name: "Chloe", moveSpeed: 60).save(args)
        new Human(name: "Kotlin Ken").save(args)

        new GrailsTeamMember(name: "Nero").save(args)
        new GrailsTeamMember(name: "Colin").save(args)
        new GrailsTeamMember(name: "Graeme").save(args)
        new GrailsTeamMember(name: "Jack").save(args)
        new GrailsTeamMember(name: "James").save(args)
        new GrailsTeamMember(name: "Ryan").save(args)
        new GrailsTeamMember(name: "Matthew").save(args)
        new GrailsTeamMember(name: "Will").save(args)
        new GrailsTeamMember(name: "Alvaro").save(args)
        new GrailsTeamMember(name: "Dave").save(args)
        new GrailsTeamMember(name: "Ivan").save(args)
        new GrailsTeamMember(name: "Jeff").save(args)
        new GrailsTeamMember(name: "Paul").save(args)
        new GrailsTeamMember(name: "Ben").save(args)
        new GrailsTeamMember(name: "Sergio").save(args)
        new GrailsTeamMember(name: "Zack").save(args)
    }
    def destroy = {
    }
}
