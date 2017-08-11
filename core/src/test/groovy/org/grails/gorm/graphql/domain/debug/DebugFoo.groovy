package org.grails.gorm.graphql.domain.debug

import grails.gorm.annotation.Entity

@Entity
class DebugFoo {

    static hasMany = [items: DebugFooItem]

}
