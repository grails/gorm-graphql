package org.grails.gorm.graphql.domain.tomany

import grails.gorm.annotation.Entity
import org.grails.gorm.graphql.domain.toone.Enum
import org.grails.gorm.graphql.domain.toone.ManyToOne

@Entity
class ToMany {

    static hasMany = [many: Many,
                      manyToOne: ManyToOne,
                      manyToMany: ManyToMany,
                      enums: Enum,
                      strings: String]

    static graphql = true
}
