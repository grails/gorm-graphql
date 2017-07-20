package org.grails.gorm.graphql.types.output

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager

@CompileStatic
@InheritConstructors
class ShowObjectTypeBuilder extends ObjectTypeBuilder {

    GraphQLDomainPropertyManager.Builder builder = propertyManager.builder()
            .alwaysNullable()

    GraphQLPropertyType type = GraphQLPropertyType.OUTPUT
}
