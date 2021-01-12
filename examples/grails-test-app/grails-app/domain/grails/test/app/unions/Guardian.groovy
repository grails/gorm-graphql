package grails.test.app.unions


import grails.test.app.inheritance.Human
import grails.test.app.inheritance.Labradoodle
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.fetcher.impl.ClosureDataFetchingEnvironment

class Guardian {

    String name

    static constraints = {
    }

    static graphql = GraphQLMapping.build {
        addUnion('pets', 'Pets') {
            collection(true)
            type(Cat) {
                field('name', String)
                field('lives', Integer)
            }
            type(Pup) {
                field('name', String)
                field('bones', Integer)
            }
            dataFetcher { Guardian source, ClosureDataFetchingEnvironment env ->
                return [
                    new Cat(name: 'Garfield', lives: 9),
                    new Pup(name: 'Scooby', bones: 50)
                ]
            }
        }
        addUnion('random', 'TotallyDifferent', [Labradoodle, Human]) {
            collection(true)
            dataFetcher { Guardian source, ClosureDataFetchingEnvironment env ->
                return Labradoodle.list() + Human.list()
            }
        }
    }
}

