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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VM208BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public class VM208BindingConstants {

    private static final String BINDING_ID = "vm208";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VM208INT = new ThingTypeUID(BINDING_ID, "vm208int");
    public static final ThingTypeUID THING_TYPE_VM208EX = new ThingTypeUID(BINDING_ID, "vm208ex");

    // List of all Channel Group ids
    public static final String RELAY_1 = "relay1";
    public static final String RELAY_2 = "relay2";
    public static final String RELAY_3 = "relay3";
    public static final String RELAY_4 = "relay4";
    public static final String RELAY_5 = "relay5";
    public static final String RELAY_6 = "relay6";
    public static final String RELAY_7 = "relay7";
    public static final String RELAY_8 = "relay8";

    // List of all Channel ids
    public static final String RELAY = "relay";
    public static final String LED = "led";
    public static final String BUTTON = "button";

    // List of all Configuration parameters
    /// VM208 INT
    public static final String ADDRESS = "address";
    public static final String BUSNUMBER = "busNumber";
    public static final String INTERRUPTPIN = "interruptPin";

    /// VM208 EX
    public static final String LEDREFLECTSRELAYSTATUS = "ledReflectsRelayStatus";
    public static final String SOCKET = "socket";
}
