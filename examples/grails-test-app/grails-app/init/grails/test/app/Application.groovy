package grails.test.app

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.grails.gorm.graphql.binding.manager.GraphQLDataBinderManager
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager

class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void doWithApplicationContext() {
        def binderManager = applicationContext.getBean('graphQLDataBinderManager', GraphQLDataBinderManager)
        binderManager.register(User, new UserDataBinder())
    }
}