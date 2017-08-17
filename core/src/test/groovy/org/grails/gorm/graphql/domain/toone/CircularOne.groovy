package org.grails.gorm.graphql.domain.toone

import grails.gorm.annotation.Entity

@Entity
class CircularOne {

    CircularOne one
}
