== Standalone Projects

For standalone projects, it is up to the developer how and when the schema gets created. A mapping context is required to create a schema. The mapping context is available on all link:{gormapi}/org/grails/datastore/mapping/core/Datastore.html#getMappingContext()[datastore implementations]. See the link:http://gorm.grails.org/latest/hibernate/manual/index.html#outsideGrails[GORM documentation] for information on creating a datastore.

The example below is the simplest way possible to generate a schema.

[source, groovy]
----
import org.grails.datastore.mapping.model.MappingContext
import org.grails.gorm.graphql.Schema
import graphql.schema.GraphQLSchema

MappingContext mappingContext = ...
GraphQLSchema schema = new Schema(mappingContext).generate()
----

Refer to the link:http://graphql-java.readthedocs.io/en/stable/[graphql-java] documentation on how to use the schema to execute queries and mutations against it.

== Grails Projects

For Grails projects, the schema is created automatically at startup. A spring bean is created for the schema called "graphQLSchema".

== Using The Schema

By default, no domain classes will be a part of the generated schema. It is an **opt in** functionality that requires you to explicitly state which domain classes will be a part of the schema.

The simplest way to include a domain class in the schema is to add the following to your domain class.

[source, groovy]
----
static graphql = true
----

Just by adding the `graphql = true` property on your domain, full CRUD capabilities will be available in the schema. For example, if the domain class is called `Book`:

- Queries
** `book(id: ..)`
** `bookList(max: .., sort: .., etc)`
** `bookCount`

- Mutations
** `bookCreate(book: {})`
** `bookUpdate(id: .., book: {})`
** `bookDelete(id: ..)`

=== Practical Example

Imagine you are building an API for a Conference. A talk can be presented
by a single speaker. A speaker can have many talks within the conference.
A typical one-to-many relationship which in http://gorm.grails.org[GORM]
could be expressed with:

[source, groovy]
.grails-app/domain/demo/Speaker.groovy
----
include::{sourcedir}/examples/grails-docs-app/grails-app/domain/demo/Speaker.groovy[tags=wholeFile]
----

<1> it exposes this domain class to the GraphQL API

[source, groovy]
.grails-app/domain/demo/Talk.groovy
----
include::{sourcedir}/examples/grails-docs-app/grails-app/domain/demo/Talk.groovy[tags=wholeFile]
----


TIP: GORM GraphQL plugin supports http://gorm.grails.org/latest/hibernate/manual/index.html#derivedProperties[Derived Properties] as illustrated in the previous example; `name` is derived property which concatenates `firstName` and `lastName`
