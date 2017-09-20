package org.grails.gorm.graphql.types.output

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager

/**
 * The class used to define which properties are available
 * when responding with an entity
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
@InheritConstructors
class ShowObjectTypeBuilder extends AbstractObjectTypeBuilder {

    GraphQLDomainPropertyManager.Builder builder = propertyManager.builder()
            .alwaysNullable()

    GraphQLPropertyType type = GraphQLPropertyType.OUTPUT
}
