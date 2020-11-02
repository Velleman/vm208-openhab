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
 * The {@link VM208Ex} interface defines the functionality of the VM208 Ex module.
 * The {@link VM208ExHandler} should implement this interface.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public interface VM208Ex {

    /**
     * Turns the relay on
     *
     * @param channel the channel number
     */
    public void turnRelayOn(int channel);

    /**
     * Is the relay on?
     *
     * @param channel the channel number
     * @return true if the relay is on
     */
    public boolean isRelayOn(int channel);

    /**
     * Turns the relay off
     *
     * @param channel the channel number
     */
    public void turnRelayOff(int channel);

    /**
     * Turns the led on
     *
     * @param channel the channel number
     */
    public void turnLedOn(int channel);

    /**
     * Is the led on?
     *
     * @param channel the channel number
     * @return true if the led is on
     */
    public boolean isLedOn(int channel);

    /**
     * Turns the led off
     *
     * @param channel the channel number
     */
    public void turnLedOff(int channel);

    /**
     * Is the button pressed?
     *
     * @param channel the channel number
     * @return true if the button is pressed
     */
    public boolean isButtonPressed(int channel);
}
