package grails.test.app

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.grails.gorm.graphql.binding.manager.GraphQLDataBinderManager

class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void doWithApplicationContext() {
        def binderManager = applicationContext.getBean('graphQLDataBinderManager', GraphQLDataBinderManager)
        binderManager.registerDataBinder(User, new UserDataBinder())
    }
}