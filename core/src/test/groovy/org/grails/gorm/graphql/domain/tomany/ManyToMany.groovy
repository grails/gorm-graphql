package org.grails.gorm.graphql.domain.tomany

import grails.gorm.annotation.Entity

@Entity
class ManyToMany {

    static hasMany = [toMany: ToMany]

    static belongsTo = [ToMany]
}
