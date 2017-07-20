package org.grails.gorm.graphql.binding

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.entity.property.manager.DefaultGraphQLDomainPropertyManager
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager

abstract class EmbeddedAwareGraphQLDataBinder implements GraphQLDataBinder {

    GraphQLDomainPropertyManager manager = new DefaultGraphQLDomainPropertyManager()

    void prependEmbeddedPropertyNames(Object object, Map data) {
        GormStaticApi api = (GormStaticApi)GormEnhancer.findStaticApi(object.class)
        PersistentEntity entity = api.gormPersistentEntity
        for (Association association: entity.associations) {
            if (association instanceof Embedded) {
                GraphQLDomainPropertyManager.Builder builder = manager.builder()
                        .excludeIdentifiers()
                        .excludeVersion()
                        .excludeTimestamps()

                for (GraphQLDomainProperty property: builder.getProperties(association.associatedEntity)) {
                    if (data.containsKey(property.name)) {
                        data.put("${association.name}.${property.name}".toString(), data.remove(property.name))
                    }
                }
            }
        }
    }
}
