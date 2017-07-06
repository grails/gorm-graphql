package org.grails.gorm.graphql.grails

import org.grails.gorm.graphql.binding.GraphQLDataBinder

class GrailsGraphQLDataBinder implements GraphQLDataBinder {

    @Override
    void bind(Object object, Map data) {
        object.properties = data
    }
}
