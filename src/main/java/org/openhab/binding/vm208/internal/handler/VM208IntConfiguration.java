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
 * The {@link VM208IntConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public class VM208IntConfiguration {

    /**
     * Address of the vm208int
     */
    private int address;

    /**
     * Bus number of the vm208int
     */
    private int busNumber;

    /**
     * Interrupt GPIO pin used
     */
    private int interruptPin;

    public int getAddress() {
        return address;
    }

    public int getBusNumber() {
        return busNumber;
    }

    public int getInterruptPin() {
        return interruptPin;
    }
}
