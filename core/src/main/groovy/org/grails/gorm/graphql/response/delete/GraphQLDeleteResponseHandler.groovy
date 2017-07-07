package org.grails.gorm.graphql.response.delete

import graphql.schema.GraphQLObjectType

/**
 * Created by jameskleeh on 7/7/17.
 */
interface GraphQLDeleteResponseHandler {

    GraphQLObjectType getObjectType()
}