/*
 * A Minecraft economy plugin that enables players to hold multiple bank accounts.
 * Copyright © 2023–2026 Cloudnode OÜ.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <https://www.gnu.org/licenses/>.
 */

package pro.cloudnode.smp.bankaccounts;

import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

final class DatabaseInit {
    /**
     * Current database version.
     */
    private static final int VERSION = 0;

    private final @NotNull DataSource dataSource;

    public DatabaseInit(final @NotNull DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Ensures the database is initialised.
     */
    public void init() {
        try {
            if (!isInitialised()) {
                runScript("init");
                return;
            }

            final int version = getVersion();

            if (version == VERSION) {
                return;
            }

            if (version > VERSION) {
                throw new IllegalStateException(String.format("Unsupported database detected (%s). Only up to version %s is supported.",
                        version,
                        VERSION
                ));
            }

            int migration = version + 1;
            while (migration <= VERSION) {
                runScript(String.format("v%d", migration));
                ++migration;
            }

        } catch (final SQLException | IOException e) {
            throw new IllegalStateException("Failed to initialise database", e);
        }
    }

    /**
     * Checks whether the db is initialised based on presence of the {@code bank_meta} table.
     */
    private boolean isInitialised() throws SQLException {
        final String metaTable = "bank_meta";
        try (final Connection conn = dataSource.getConnection()) {
            try (final ResultSet rs = conn.getMetaData().getTables(null, null, metaTable, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }

            try (final ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    final String name = rs.getString("TABLE_NAME");
                    if (name != null && name.equalsIgnoreCase(metaTable)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /**
     * Gets the database version.
     */
    private int getVersion() throws SQLException {
        try (final Connection conn = dataSource.getConnection();
             final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT val FROM bank_meta WHERE name = 'version'")) {
            if (!rs.next()) {
                throw new IllegalStateException("Version is not set in database metadata");
            }
            return rs.getInt(1);
        }
    }

    /**
     * Runs an SQL script from {@code resources/db/NAME.sql}.
     *
     * @param name SQL script name (excluding extension).
     */
    private void runScript(final @NotNull String name) throws IOException, SQLException {
        try (final Connection conn = dataSource.getConnection();
             final InputStream stream = getClass().getClassLoader()
                     .getResourceAsStream(String.format("db/%s.sql", name))) {
            if (stream == null) {
                throw new IllegalStateException(String.format("db/%s.sql not found on classpath", name));
            }

            final boolean auto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream,
                    StandardCharsets.UTF_8
            ));
                 final Statement stmt = conn.createStatement()) {
                final StringBuilder sql = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.isBlank() || line.startsWith("--")) {
                        continue;
                    }

                    sql.append(line).append('\n');

                    if (line.endsWith(";")) {
                        stmt.execute(sql.toString().trim().replaceAll(";$", ""));
                        sql.setLength(0);
                    }
                }

                if (!sql.isEmpty()) {
                    stmt.execute(sql.toString());
                }

                conn.commit();
            } catch (final SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(auto);
            }
        }
    }
}
