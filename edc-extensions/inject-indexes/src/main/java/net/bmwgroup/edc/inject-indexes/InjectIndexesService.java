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

import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.eclipse.edc.connector.store.sql.transferprocess.store.schema.TransferProcessStoreStatements;
import org.eclipse.edc.connector.store.sql.contractnegotiation.store.schema.ContractNegotiationStatements;

import java.sql.SQLException;

import static java.lang.String.format;

/**
 * Class to register DDTR Assets
 */
public class InjectIndexesService {

    private final Monitor monitor;

    private final DataSourceRegistry dataSourceRegistry;
    private final TransactionContext transactionContext;

    private final String tpDatasourceName;
    private final String cnDatasourceName;

    private final TransferProcessStoreStatements tpStatements;
    private final ContractNegotiationStatements cnStatements;

    public InjectIndexesService(DataSourceRegistry dataSourceRegistry, String tpDatasourceName,
            String cnDatasourceName, TransactionContext transactionContext, TransferProcessStoreStatements tpStatements,
            ContractNegotiationStatements cnStatements, Monitor monitor) {
        this.dataSourceRegistry = dataSourceRegistry;
        this.tpDatasourceName = tpDatasourceName;
        this.cnDatasourceName = cnDatasourceName;
        this.transactionContext = transactionContext;
        this.tpStatements = tpStatements;
        this.cnStatements = cnStatements;
        this.monitor = monitor;
    }

    public void createIndexes() {
        monitor.debug("Grabing connections.");
        transactionContext.execute(() -> {
            try(var tps_connection = dataSourceRegistry.resolve(tpDatasourceName).getConnection();) {

                tps_connection.createStatement()
                        .executeUpdate(format("CREATE INDEX IF NOT EXISTS transfer_process_state ON %s ( %s , %s)",
                                tpStatements.getTransferProcessTableName(),
                                tpStatements.getStateColumn(),
                                tpStatements.getStateTimestampColumn()));

                monitor.debug("executed statement");
            } catch (SQLException exception) {
                throw new EdcPersistenceException(exception);
            } catch (Exception e) {
                monitor.severe("Failed to inject transfer process state index.", e);
            }
        });

        transactionContext.execute(() -> {
            try(var cn_connection = dataSourceRegistry.resolve(cnDatasourceName).getConnection();) {

                cn_connection.createStatement()
                        .executeUpdate(format("CREATE INDEX IF NOT EXISTS contract_negotiation_state ON %s ( %s , %s)",
                                cnStatements.getContractNegotiationTable(),
                                cnStatements.getStateColumn(),
                                cnStatements.getStateTimestampColumn()));

                monitor.debug("executed statement");
            } catch (SQLException exception) {
                throw new EdcPersistenceException(exception);
            } catch (Exception e) {
                monitor.severe("Failed to inject contract negotiation state index.", e);
            }
        });
    }
}
