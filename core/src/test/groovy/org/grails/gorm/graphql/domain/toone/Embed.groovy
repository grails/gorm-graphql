package org.grails.gorm.graphql.domain.toone

import grails.gorm.annotation.Entity
import org.grails.gorm.graphql.domain.tomany.Many

@Entity
class Embed {

    One one

    static hasMany = [many: Many]
}
