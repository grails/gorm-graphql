package org.grails.gorm.graphql.entity

import graphql.language.Selection
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.ToMany
import org.grails.datastore.mapping.model.types.ToOne
import graphql.language.Field

/**
 * Helper class to determine which properties should be eagerly
 * fetched based on the fields in a {@link graphql.schema.DataFetchingEnvironment}.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class EntityFetchOptions {

    Map<String, Association> associations = [:]
    protected PersistentEntity entity
    protected Set<String> associationNames
    protected String propertyName

    EntityFetchOptions(PersistentEntity entity) {
        this(entity, null)
    }

    EntityFetchOptions(PersistentEntity entity, String projectionName) {
        if (entity == null) {
            throw new IllegalArgumentException('Cannot retrieve fetch options for a null entity. Is GORM initialized?')
        }
        this.entity = entity
        this.propertyName = projectionName
        for (Association association: entity.associations) {
            associations.put(association.name, association)
        }
        associationNames = associations.keySet()
    }

    protected boolean isForeignKeyInChild(Association association) {
        association instanceof ToOne && ((ToOne)association).foreignKeyInChild || association instanceof ToMany
    }

    @SuppressWarnings(['NestedBlockDepth'])
    protected void handleField(String parentName, Field selectedField, Set<String> joinProperties) {
        String resolvedName
        if (parentName) {
            resolvedName = parentName + '.' + selectedField.name
        }
        else {
            resolvedName = selectedField.name
        }

        if (associationNames.contains(selectedField.name)) {
            Association association = associations.get(selectedField.name)
            PersistentEntity entity = association.associatedEntity

            if (entity == null) {
                joinProperties.add(resolvedName)
                return
            }

            List<Selection> selections = selectedField.selectionSet?.selections

            if (!association.embedded) {
                if (isForeignKeyInChild(association)) {
                    joinProperties.add(resolvedName)
                }
                else if (selections?.size() == 1 && selections[0] instanceof Field) {
                    Field field = (Field)selections[0]
                    if (!entity.isIdentityName(field.name)) {
                        joinProperties.add(resolvedName)
                    }
                }
                else {
                    joinProperties.add(resolvedName)
                }
            }

            List<Field> fields = []
            for (Selection selection: selections) {
                if (selection instanceof Field) {
                    Field field = (Field) selection
                    if (field.name == association.referencedPropertyName) {
                        for (Selection nestedSelection: field.selectionSet?.selections) {
                            if (nestedSelection instanceof Field) {
                                handleField(parentName, (Field)nestedSelection, joinProperties)
                            }
                        }
                    }
                    else {
                        fields.add(field)
                    }
                }
            }

            joinProperties.addAll(new EntityFetchOptions(entity, resolvedName).getJoinProperties(fields))
        }
    }

    Set<String> getJoinProperties(List<Field> fields) {
        Set<String> joinProperties = []

        if (fields != null) {
            for (Field field: fields) {
                handleField(propertyName, field, joinProperties)
            }
        }

        joinProperties
    }

    Set<String> getJoinProperties(DataFetchingEnvironment environment) {
        List<Field> fields = []
        List<Field> environmentFields = environment.fields
        if (environmentFields != null) {
            for (Field field: environmentFields) {
                if (field.selectionSet != null) {
                    for (Selection selection: field.selectionSet.selections) {
                        if (selection instanceof Field) {
                            fields.add((Field)selection)
                        }
                    }
                }
            }
        }

        getJoinProperties(fields)
    }
}
