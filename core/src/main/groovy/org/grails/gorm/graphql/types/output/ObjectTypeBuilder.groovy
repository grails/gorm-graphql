package org.grails.gorm.graphql.types.output

import graphql.schema.GraphQLOutputType
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.types.GraphQLPropertyType

/**
 * Definition of a builder that creates output types
 *
 * @author James Kleeh
 * @since 1.0.0
 */
interface ObjectTypeBuilder {

    GraphQLOutputType build(PersistentEntity entity)

    GraphQLPropertyType getType()
}
