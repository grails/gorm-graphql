package org.grails.gorm.graphql.domain.circularassociation

import grails.gorm.annotation.Entity

@Entity
class CircularAssociationBar {
    CircularAssociationFoo foo
    
    static graphql = true
}
