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

import org.seasar.doma.DomaNullPointerException;
import org.seasar.doma.wrapper.Wrapper;

/**
 * 非永続性プロパティです。
 * 
 * @author taedium
 * 
 */
public class TransientPropertyMeta<W extends Wrapper<?>> implements
        EntityPropertyMeta<W> {

    /** 名前 */
    protected final String name;

    /** ドメイン */
    protected final W domain;

    /**
     * インスタンスを構築します。
     * 
     * @param name
     *            名前
     * @param domain
     *            ドメイン
     */
    public TransientPropertyMeta(String name, W domain) {
        if (name == null) {
            throw new DomaNullPointerException("name");
        }
        if (domain == null) {
            throw new DomaNullPointerException("domain");
        }
        this.name = name;
        this.domain = domain;
    }

    @Override
    public String getColumnName() {
        return null;
    }

    @Override
    public W getWrapper() {
        return domain;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isId() {
        return false;
    }

    @Override
    public boolean isInsertable() {
        return false;
    }

    @Override
    public boolean isUpdatable() {
        return false;
    }

    @Override
    public boolean isVersion() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public String toString() {
        return domain != null ? domain.toString() : null;
    }
}
