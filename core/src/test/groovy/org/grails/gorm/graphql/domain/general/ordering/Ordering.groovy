package org.grails.gorm.graphql.domain.general.ordering

import grails.gorm.annotation.Entity
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping

@Entity
class Ordering {
    String d
    String c
    String a
    String b
    
    // Checks default property order 
    String g
    String f
    String q
    
    static constraints = {
        a order: 4
        b order: 1
        d order: 0
        c order: -10

    }
    
    static graphql = GraphQLMapping.build {
        add("aa",String.class,{
            order(1) // same order as 'a'
        })
        property("q",[order:8])
        property("a",[order:2])
    }
}
