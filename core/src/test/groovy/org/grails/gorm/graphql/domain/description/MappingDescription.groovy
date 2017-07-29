package org.grails.gorm.graphql.domain.description

import grails.gorm.annotation.Entity
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping

@Entity
class MappingDescription {

    static graphql = GraphQLMapping.build {
        description 'MappingDescription class'
    }
}
