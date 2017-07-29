package org.grails.gorm.graphql.domain.toone

import grails.gorm.annotation.Entity

@Entity
class BelongsToHasOne {

    static belongsTo = [one: HasOne]

    static graphql = true
}
