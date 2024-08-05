/* Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.tractusx.edc.inject.indexes;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;

import org.eclipse.edc.connector.store.sql.transferprocess.SqlTransferProcessStoreExtension;
import org.eclipse.edc.connector.store.sql.contractnegotiation.SqlContractNegotiationStoreExtension;
import org.eclipse.edc.connector.store.sql.transferprocess.store.schema.TransferProcessStoreStatements;
import org.eclipse.edc.connector.store.sql.contractnegotiation.store.schema.ContractNegotiationStatements;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.sql.lease.LeaseStatements;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

/**
 * Indexes Injection Extension
 */
@Extension(value = InjectIndexesExtension.NAME)
public class InjectIndexesExtension implements ServiceExtension {

    public static final String NAME = "Inject Indexes Extension";

    @Inject
    private Monitor monitor;
    @Inject
    private DataSourceRegistry dataSourceRegistry;
    @Inject
    private TransactionContext transactionContext;

    private InjectIndexesService service;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var tpDatasourceName = context.getConfig().getString(SqlTransferProcessStoreExtension.DATASOURCE_NAME_SETTING,
                "transferprocess");
        var cnDatasourceName = context.getConfig()
                .getString(SqlContractNegotiationStoreExtension.DATASOURCE_NAME_SETTING, "contractnegotiation");
        this.service = new InjectIndexesService(dataSourceRegistry,
                tpDatasourceName,
                cnDatasourceName,
                transactionContext,
                new org.eclipse.edc.connector.store.sql.transferprocess.store.schema.postgres.PostgresDialectStatements(),
                new org.eclipse.edc.connector.store.sql.contractnegotiation.store.schema.postgres.PostgresDialectStatements(),
                monitor);
    }

    @Override
    public void start() {
        try {
            service.createIndexes();
        } catch (Exception e) {
            monitor.warning("Failed to inject indexes.", e);
        }

    }

}
