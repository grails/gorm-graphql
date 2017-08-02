package org.grails.gorm.graphql.domain.tomany

import grails.gorm.annotation.Entity
import org.grails.gorm.graphql.domain.toone.One

@Entity
class Many {
    One one
}
