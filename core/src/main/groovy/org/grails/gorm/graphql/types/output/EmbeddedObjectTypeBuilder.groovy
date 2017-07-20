package org.grails.gorm.graphql.types.output

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager

@CompileStatic
@InheritConstructors
class EmbeddedObjectTypeBuilder extends ObjectTypeBuilder {

    GraphQLDomainPropertyManager.Builder builder = propertyManager.builder()
            .alwaysNullable()
            .excludeIdentifiers()
            .excludeVersion()

    GraphQLPropertyType type = GraphQLPropertyType.OUTPUT_EMBEDDED
}
