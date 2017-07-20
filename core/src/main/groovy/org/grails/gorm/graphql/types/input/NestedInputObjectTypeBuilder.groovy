package org.grails.gorm.graphql.types.input

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager
import org.grails.gorm.graphql.types.GraphQLTypeManager

@CompileStatic
class NestedInputObjectTypeBuilder extends InputObjectTypeBuilder {

    NestedInputObjectTypeBuilder(GraphQLDomainPropertyManager propertyManager, GraphQLTypeManager typeManager, GraphQLPropertyType type) {
        super(propertyManager, typeManager)
        this.type = type
    }

    GraphQLDomainPropertyManager.Builder builder
    GraphQLPropertyType type

    {
        builder = propertyManager.builder()
                .excludeTimestamps()
                .excludeVersion()
                .alwaysNullable()
                .condition { PersistentProperty prop ->
                    if (prop instanceof Association) {
                        Association association = (Association)prop
                        (association.owningSide || !association.bidirectional) && !association.circular
                    } else {
                        true
                    }
                }
    }

}
