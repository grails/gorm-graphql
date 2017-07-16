package org.grails.gorm.graphql.response.errors

import graphql.schema.GraphQLFieldDefinition

/**
 * Responsible for defining what data is available in a response
 * to return validation errors to the user
 *
 * @author James Kleeh
 * @since 1.0.0
 */
interface GraphQLErrorsResponseHandler {

    GraphQLFieldDefinition getFieldDefinition()
}
