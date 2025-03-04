/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.vm208.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VM208ExConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public class VM208ExConfiguration {

    /**
     * Socket of the VM208Ex
     */
    private int socket;

    /**
     * Led reflects status of Relay?
     */
    private boolean ledReflectsRelayStatus;

    public int getSocket() {
        return socket;
    }

    public boolean isLedReflectsRelayStatus() {
        return ledReflectsRelayStatus;
    }
}
