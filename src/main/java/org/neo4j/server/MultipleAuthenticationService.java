/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;

/**
 * @author tbaum
 * @since 16.04.11 15:38
 */
public class MultipleAuthenticationService implements AuthenticationService {

    private final PropertiesConfiguration configuration;
    private final File configFile;

    MultipleAuthenticationService(File configFile) {
        this.configFile = configFile;
        try {
            this.configuration = new PropertiesConfiguration(configFile);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPermissionForUser(String user, Permission permission) {
        if (permission == Permission.NONE) {
            configuration.clearProperty(user);
        } else {
            configuration.setProperty(user, permission.name().toLowerCase());
        }
        try {
            configuration.save(configFile);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasAccess(String method, final byte[] credentials) {
        final String cred = new String(credentials);
        final String rights = configuration.getString(cred, "");

        return isVerb(method, "PUT", "POST", "DELETE") && rights.contains("w") ||
                isVerb(method, "GET") && rights.contains("r");
    }

    private boolean isVerb(String method, final String... verbs) {
        for (String verb : verbs) {
            if (verb.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    public enum Permission {
        NONE, RO, RW
    }
}
