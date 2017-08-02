package org.grails.gorm.graphql.domain.toone

import grails.gorm.annotation.Entity
import org.grails.gorm.graphql.domain.tomany.ToMany

@Entity
class ManyToOne {

    One one

    static belongsTo = [toMany: ToMany]

    static graphql = true
}
