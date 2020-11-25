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
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VM208BusHandler} class is defined to lock the bus.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
@Component()
public class VM208BusHandler {

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(VM208BusHandler.class);

    public synchronized void claimBus(Runnable command) {
        command.run();
    }
}
