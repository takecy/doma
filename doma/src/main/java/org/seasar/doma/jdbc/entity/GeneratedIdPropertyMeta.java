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
package org.seasar.doma.jdbc.entity;

import java.sql.Statement;

import org.seasar.doma.DomaNullPointerException;
import org.seasar.doma.GenerationType;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.id.IdGenerationConfig;
import org.seasar.doma.jdbc.id.IdGenerator;
import org.seasar.doma.message.DomaMessageCode;
import org.seasar.doma.wrapper.NumberWrapper;

/**
 * 生成される識別子のプロパティです。
 * 
 * @author taedium
 * 
 */
public class GeneratedIdPropertyMeta<W extends NumberWrapper<?>> extends
        BasicPropertyMeta<W> {

    /** 識別子のジェネレータ */
    protected final IdGenerator idGenerator;

    /**
     * インスタンスを構築します。
     * 
     * @param name
     *            名前
     * @param columnName
     *            カラム名
     * @param wrapper
     *            ドメイン
     * @param idGenerator
     *            ジェネレータ
     * @throws DomaNullPointerException
     *             {@code idGenerator} が {@code null} の場合
     */
    public GeneratedIdPropertyMeta(String name, String columnName, W wrapper,
            IdGenerator idGenerator) {
        super(name, columnName, wrapper, true, true);
        if (idGenerator == null) {
            throw new DomaNullPointerException("idGenerator");
        }
        this.idGenerator = idGenerator;
    }

    @Override
    public boolean isId() {
        return true;
    }

    /**
     * 識別子を生成する方法を検証します。
     * 
     * @param config
     *            識別子生成の設定
     * @throws JdbcException
     *             識別子を生成する方法がサポートされてない場合
     */
    public void validateGenerationStrategy(IdGenerationConfig config) {
        Dialect dialect = config.getDialect();
        GenerationType generationType = idGenerator.getGenerationType();
        if (!isGenerationTypeSupported(generationType, dialect)) {
            EntityMeta<?> entity = config.getEntityMeta();
            throw new JdbcException(DomaMessageCode.DOMA2021, entity.getName(),
                    name, generationType.name(), dialect.getName());
        }
    }

    /**
     * 方言で識別子を生成する方法がサポートされていれば {@code true} を返します。
     * 
     * @param generationType
     *            識別子を生成する方法
     * @param dialect
     *            方言
     * @return サポートされていれば {@code true}
     */
    protected boolean isGenerationTypeSupported(GenerationType generationType,
            Dialect dialect) {
        switch (generationType) {
        case IDENTITY: {
            return dialect.supportsIdentity();
        }
        case SEQUENCE: {
            return dialect.supportsSequence();
        }
        }
        return true;
    }

    /**
     * バッチ処理をサポートしているかどうかを返します。
     * 
     * @param config
     *            識別子生成の設定
     * @return サポートしている場合 {@code true}
     */
    public boolean isIncluded(IdGenerationConfig config) {
        return idGenerator.includesIdentityColumn(config);
    }

    /**
     * バッチ処理をサポートしているかどうかを返します。
     * 
     * @param config
     *            識別子生成の設定
     * @return サポートしている場合 {@code true}
     */
    public boolean isBatchSupported(IdGenerationConfig config) {
        return idGenerator.supportsBatch(config);
    }

    /**
     * {@link Statement#getGeneratedKeys()} をサポートしているかどうかを返します。
     * 
     * @param config
     *            識別子生成の設定
     * @return サポートしている場合 {@code true}
     */
    public boolean isAutoGeneratedKeysSupported(IdGenerationConfig config) {
        return idGenerator.supportsAutoGeneratedKeys(config);
    }

    /**
     * INSERTの実行前に識別子を設定します。
     * 
     * @param config
     *            識別子生成の設定
     */
    public void preInsert(IdGenerationConfig config) {
        Long value = idGenerator.generatePreInsert(config);
        if (value != null) {
            wrapper.set(value);
        }
    }

    /**
     * INSERTの実行後に識別子を設定します。
     * 
     * @param config
     *            識別子生成の設定
     * @param statement
     *            INSERT文を実行した文
     */
    public void postInsert(IdGenerationConfig config, Statement statement) {
        Long value = idGenerator.generatePostInsert(config, statement);
        if (value != null) {
            wrapper.set(value);
        }
    }
}
