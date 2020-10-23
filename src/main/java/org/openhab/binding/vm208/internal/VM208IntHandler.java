/**
 * Copyright (c) 2020-2020 Contributors to the openHAB project
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

import static org.openhab.binding.vm208.internal.VM208BindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vm208.internal.i2c.TCA9544GpioProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * The {@link VM208IntHandler} is responsible as gateway for 4 modules.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public class VM208IntHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private @NonNullByDefault({}) Integer busNumber;
    private @NonNullByDefault({}) Integer address;

    private VM208BaseHandler[] sockets;

    private @NonNullByDefault({}) TCA9544GpioProvider tcaProvider;

    public VM208IntHandler(Bridge bridge) {
        super(bridge);

        this.sockets = new VM208BaseHandler[4];
    }

    public void registerSocket(VM208BaseHandler vm208baseHandler) {
        int socket = vm208baseHandler.getSocket();
        if (this.sockets[socket] != null) {
            this.sockets[socket] = vm208baseHandler;
        } else {
            throw new IllegalStateException("Socket already taken");
        }
    }

    public void unregisterSocket(VM208BaseHandler vm208baseHandler) {
        int socket = vm208baseHandler.getSocket();
        if (this.sockets[socket] == null) {
            throw new IllegalStateException("Socket has not been registered.");
        } else {
            this.sockets[socket] = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no channels
    }

    @Override
    public void initialize() {
        try {
            checkConfiguration();
            tcaProvider = initializeTcaProvider();
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException | SecurityException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "An exception occurred while adding pin. Check pin configuration. Exception: " + e.getMessage());
        }
    }

    private @Nullable TCA9544GpioProvider initializeTcaProvider() {
        TCA9544GpioProvider tca = null;
        logger.debug("Initializing tca provider for busNumber {} and address {}", busNumber, address);
        try {
            tca = new TCA9544GpioProvider(busNumber, address);
        } catch (UnsupportedBusNumberException | IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Tried to access not available I2C bus: " + ex.getMessage());
        }
        logger.debug("Got tcaProvider {}", tca);
        return tca;
    }

    protected void checkConfiguration() {
        Configuration configuration = getConfig();
        address = Integer.parseInt((configuration.get(ADDRESS)).toString(), 16);
        busNumber = Integer.parseInt((configuration.get(BUSNUMBER)).toString());
    }

    public synchronized void sendToSocket(VM208BaseHandler vm208baseHandler, Runnable command) {
        int socket = vm208baseHandler.getSocket();
        try {
            if (tcaProvider.currentChannel() != socket) {
                tcaProvider.changeChannel((byte) socket);
            }
            try {
                command.run();
            } catch (Exception ex) {
                logger.error(ex.toString());
            }
        } catch (IOException exception) {

        } finally {
            try {
                tcaProvider.changeChannel((byte) 0);
            } catch (IOException exception) {

            }
        }
    }

    @Override
    public void dispose() {
        // dispose sockets?

        super.dispose();
    }
}
