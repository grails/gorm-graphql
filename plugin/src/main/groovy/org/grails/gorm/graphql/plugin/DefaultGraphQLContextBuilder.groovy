package org.grails.gorm.graphql.plugin

import org.grails.web.servlet.mvc.GrailsWebRequest

class DefaultGraphQLContextBuilder implements GraphQLContextBuilder {

    @Override
    Map buildContext(GrailsWebRequest request) {
        [locale: request.locale]
    }
}
