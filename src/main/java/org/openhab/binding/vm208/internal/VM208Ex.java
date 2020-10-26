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
package org.openhab.binding.vm208.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VM208Ex} interface defines the functionality of the VM208 Ex module.
 * The {@link VM208ExHandler} should implement this interface.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public interface VM208Ex {
    public void turnRelayOn(int channel);

    public boolean isRelayOn(int channel);

    public void turnRelayOff(int channel);

    public void turnLedOn(int channel);

    public boolean isLedOn(int channel);

    public void turnLedOff(int channel);

    public boolean isButtonPressed(int channel);
}
