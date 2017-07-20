package org.grails.gorm.graphql.types.input

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager
import org.grails.gorm.graphql.types.GraphQLTypeManager

@CompileStatic
class EmbeddedInputObjectTypeBuilder extends InputObjectTypeBuilder {

    boolean overrideNull

    EmbeddedInputObjectTypeBuilder(GraphQLDomainPropertyManager propertyManager, GraphQLTypeManager typeManager, boolean overrideNull) {
        super(propertyManager, typeManager)
        this.overrideNull = overrideNull
    }

    GraphQLDomainPropertyManager.Builder builder
    GraphQLPropertyType type

    {
        builder = propertyManager.builder()
                .excludeTimestamps()
                .excludeVersion()
                .excludeIdentifiers()
                .condition { PersistentProperty prop ->
                    if (prop instanceof Association) {
                        Association association = (Association)prop
                        association.owningSide || !association.bidirectional
                    } else {
                        true
                    }
                }

        if (overrideNull) {
            builder.alwaysNullable()
            type = GraphQLPropertyType.UPDATE_EMBEDDED
        }
        else {
            type = GraphQLPropertyType.CREATE_EMBEDDED
        }
    }


}
