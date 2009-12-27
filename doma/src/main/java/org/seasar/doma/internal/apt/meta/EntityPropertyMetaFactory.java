/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.internal.apt.meta;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.seasar.doma.GeneratedValue;
import org.seasar.doma.Id;
import org.seasar.doma.Version;
import org.seasar.doma.internal.apt.AptException;
import org.seasar.doma.internal.apt.AptIllegalStateException;
import org.seasar.doma.internal.apt.mirror.ColumnMirror;
import org.seasar.doma.internal.apt.mirror.SequenceGeneratorMirror;
import org.seasar.doma.internal.apt.mirror.TableGeneratorMirror;
import org.seasar.doma.internal.apt.type.BasicType;
import org.seasar.doma.internal.apt.type.DomainType;
import org.seasar.doma.internal.apt.util.ElementUtil;
import org.seasar.doma.internal.apt.util.TypeMirrorUtil;
import org.seasar.doma.internal.message.DomaMessageCode;

/**
 * 
 * @author taedium
 * 
 */
public class EntityPropertyMetaFactory {

    protected final ProcessingEnvironment env;

    public EntityPropertyMetaFactory(ProcessingEnvironment env) {
        assertNotNull(env);
        this.env = env;
    }

    public EntityPropertyMeta createEntityPropertyMeta(
            VariableElement fieldElement, EntityMeta entityMeta) {
        assertNotNull(fieldElement, entityMeta);
        TypeElement entityElement = ElementUtil.toTypeElement(fieldElement
                .getEnclosingElement(), env);
        if (entityElement == null) {
            throw new AptIllegalStateException(fieldElement.toString());
        }
        EntityPropertyMeta propertyMeta = new EntityPropertyMeta(entityElement,
                fieldElement, entityMeta.getNamingType(), entityElement
                        .equals(entityMeta.getEntityElement()), env);
        doName(propertyMeta, fieldElement, entityMeta);
        doId(propertyMeta, fieldElement, entityMeta);
        doVersion(propertyMeta, fieldElement, entityMeta);
        doColumn(propertyMeta, fieldElement, entityMeta);
        doDataType(propertyMeta, fieldElement, entityMeta);
        return propertyMeta;
    }

    protected void doId(EntityPropertyMeta propertyMeta,
            VariableElement fieldElement, EntityMeta entityMeta) {
        Id id = fieldElement.getAnnotation(Id.class);
        if (id == null) {
            return;
        }
        if (entityMeta.hasGeneratedIdPropertyMeta()) {
            throw new AptException(DomaMessageCode.DOMA4036, env, fieldElement);
        }
        propertyMeta.setId(true);
        GeneratedValue generatedValue = fieldElement
                .getAnnotation(GeneratedValue.class);
        if (generatedValue == null) {
            return;
        }
        if (entityMeta.hasGeneratedIdPropertyMeta()) {
            throw new AptException(DomaMessageCode.DOMA4037, env, fieldElement);
        }
        switch (generatedValue.strategy()) {
        case IDENTITY:
            doIdentityIdGeneratorMeta(propertyMeta, fieldElement, entityMeta);
            break;
        case SEQUENCE:
            doSequenceIdGeneratorMeta(propertyMeta, fieldElement, entityMeta);
            break;
        case TABLE:
            doTableIdGeneratorMeta(propertyMeta, fieldElement, entityMeta);
            break;
        default:
            assertUnreachable();
            break;
        }
    }

    protected void doIdentityIdGeneratorMeta(EntityPropertyMeta propertyMeta,
            VariableElement fieldElement, EntityMeta entityMeta) {
        propertyMeta.setIdGeneratorMeta(new IdentityIdGeneratorMeta());
    }

    protected void doSequenceIdGeneratorMeta(EntityPropertyMeta propertyMeta,
            VariableElement fieldElement, EntityMeta entityMeta) {
        SequenceGeneratorMirror sequenceGeneratorMirror = SequenceGeneratorMirror
                .newInstance(fieldElement, env);
        if (sequenceGeneratorMirror == null) {
            throw new AptException(DomaMessageCode.DOMA4034, env, fieldElement);
        }
        SequenceIdGeneratorMeta idGeneratorMeta = new SequenceIdGeneratorMeta(
                sequenceGeneratorMirror);
        propertyMeta.setIdGeneratorMeta(idGeneratorMeta);
    }

    protected void doTableIdGeneratorMeta(EntityPropertyMeta propertyMeta,
            VariableElement fieldElement, EntityMeta entityMeta) {
        TableGeneratorMirror tableGeneratorMirror = TableGeneratorMirror
                .newInstance(fieldElement, env);
        if (tableGeneratorMirror == null) {
            throw new AptException(DomaMessageCode.DOMA4035, env, fieldElement);
        }
        TableIdGeneratorMeta idGeneratorMeta = new TableIdGeneratorMeta(
                tableGeneratorMirror);
        propertyMeta.setIdGeneratorMeta(idGeneratorMeta);
    }

    protected void doName(EntityPropertyMeta propertyMeta,
            VariableElement fieldElement, EntityMeta entityMeta) {
        String name = fieldElement.getSimpleName().toString();
        if (name.startsWith(MetaConstants.RESERVED_NAME_PREFIX)) {
            throw new AptException(DomaMessageCode.DOMA4025, env, fieldElement,
                    MetaConstants.RESERVED_NAME_PREFIX);
        }
        propertyMeta.setName(name);
    }

    protected void doVersion(EntityPropertyMeta propertyMeta,
            VariableElement fieldElement, EntityMeta entityMeta) {
        Version version = fieldElement.getAnnotation(Version.class);
        if (version != null) {
            if (entityMeta.hasVersionPropertyMeta()) {
                throw new AptException(DomaMessageCode.DOMA4024, env,
                        fieldElement);
            }
            TypeMirror referenceType = TypeMirrorUtil.boxIfPrimitive(
                    fieldElement.asType(), env);
            if (!TypeMirrorUtil.isAssignable(referenceType, Number.class, env)) {
                throw new AptException(DomaMessageCode.DOMA4093, env,
                        fieldElement);
            }
            propertyMeta.setVersion(true);
        }
    }

    protected void doColumn(EntityPropertyMeta propertyMeta,
            VariableElement fieldElement, EntityMeta entityMeta) {
        ColumnMirror columnMirror = ColumnMirror.newInstance(fieldElement, env);
        if (columnMirror == null) {
            return;
        }
        if (propertyMeta.isId() || propertyMeta.isVersion()) {
            if (!columnMirror.getInsertableValue()) {
                throw new AptException(DomaMessageCode.DOMA4088, env,
                        fieldElement, columnMirror.getAnnotationMirror(),
                        columnMirror.getInsertable());
            }
            if (!columnMirror.getUpdatableValue()) {
                throw new AptException(DomaMessageCode.DOMA4089, env,
                        fieldElement, columnMirror.getAnnotationMirror(),
                        columnMirror.getUpdatable());
            }
        }
        propertyMeta.setColumnMirror(columnMirror);
    }

    protected void doDataType(EntityPropertyMeta propertyMeta,
            VariableElement fieldElement, EntityMeta entityMeta) {
        TypeMirror type = fieldElement.asType();
        DomainType domainType = DomainType.newInstance(type, env);
        if (domainType != null) {
            propertyMeta.setDataType(domainType);
        } else {
            BasicType basicType = BasicType.newInstance(type, env);
            if (basicType != null) {
                propertyMeta.setDataType(basicType);
            } else {
                throw new AptException(DomaMessageCode.DOMA4096, env,
                        fieldElement, type);
            }
        }
    }

    protected boolean isNumber(TypeMirror typeMirror) {
        return TypeMirrorUtil.isAssignable(typeMirror, Number.class, env);
    }

}
