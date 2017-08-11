package org.grails.gorm.graphql.domain.circularassociation

import grails.gorm.annotation.Entity

@Entity
class CircularAssociationFoo {
    CircularAssociationBar bar
}
