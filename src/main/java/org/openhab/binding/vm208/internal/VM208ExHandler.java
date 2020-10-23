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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.mapdb.Atomic.Integer;
import org.mapdb.Atomic.String;
import org.openhab.binding.vm208.internal.i2c.GPIODataHolder;
import org.openhab.binding.vm208.internal.i2c.PinStateHolder;
import org.openhab.binding.vm208.internal.i2c.TCA6424AGpioProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * The {@link VM208ExHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
public class VM208ExHandler extends BaseThingHandler implements VM208BaseHandler, VM208Ex {

    private final Logger logger = LoggerFactory.getLogger(VM208ExHandler.class);

    private @Nullable VM208ExConfiguration config;

    private @Nullable VM208IntHandler gateway;

    private TCA6424AGpioProvider mcpProvider;
    private PinStateHolder pinStateHolder;

    private int socket;
    private boolean ledReflectsRelayStatus;

    private static final Pin[] RELAY_PIN_MAP = new Pin[] { TCA_00, TCA_01, TCA_02, TCA_03, TCA_04, TCA_05, TCA_06,
            TCA_07 };

    private static final Pin[] BUTTON_PIN_MAP = new Pin[] { TCA_10, TCA_11, TCA_12, TCA_13, TCA_14, TCA_15, TCA_16,
            TCA_17 };

    private static final Pin[] LED_PIN_MAP = new Pin[] { TCA_20, TCA_21, TCA_22, TCA_23, TCA_24, TCA_25, TCA_26,
            TCA_27 };

    public VM208ExHandler(Thing thing) {
        super(thing);

        mcpProvider = initializeTcaProvider();
        pinStateHolder = new PinStateHolder(mcpProvider, thing);

        gateway = (VM208IntHandler) this.getBridge().getHandler();
    }

    private TCA6424AGpioProvider initializeTcaProvider() {
        TCA6424AGpioProvider tca = null;
        logger.debug("initializing tca provider for busNumber {} and address {}", busNumber, address);
        try {
            mcp = new TCA6424AGpioProvider(busNumber, address);
        } catch (UnsupportedBusNumberException | IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Tried to access not available I2C bus: " + ex.getMessage());
        }
        logger.debug("got tcaProvider {}", mcp);
        return mcp;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command: {} on channelGroup {} on channel {}", command.toFullString(),
                channelUID.getGroupId(), channelUID.getIdWithoutGroup());

        String[] relayChannels = new String[] { RELAY_1, RELAY_2, RELAY_3, RELAY_4, RELAY_5, RELAY_6, RELAY_7,
                RELAY_8 };

        for (int i = 1; i < relayChannels.length; i++) {
            String relayChannel = relayChannels[i];
            if (relayChannel.equals(channelUID.getGroupId())) {
                switch (channelUID.getIdWithoutGroup()) {
                    case RELAY:
                        if (command instanceof OnOffType) {
                            if (command.equals(OnOffType.ON)) {
                                this.turnRelayOn(i - 1);
                            } else {
                                this.turnRelayOff(i - 1);
                            }
                        }
                        break;
                    case BUTTON:
                        break;
                    case LED:
                        if (command.equals(OnOffType.ON)) {
                            this.turnLedOn(i - 1);
                        } else {
                            this.turnLedOff(i - 1);
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(VM208ExConfiguration.class);

        updateStatus(ThingStatus.OFFLINE);
    }

    protected void checkConfiguration() {
        Configuration configuration = getConfig();
        address = Integer.parseInt((configuration.get(ADDRESS)).toString(), 16);
        busNumber = Integer.parseInt((configuration.get(BUSNUMBER)).toString());
    }

    @Override
    public void turnRelayOn(int channel) {
        // request communication
        this.gateway.sendToSocket(this, () -> {
            this.turnRelayOn(channel);

            if (ledReflectsRelayStatus) {
                this.turnLedOn(channel);
            }
        });
    }

    private void turnRelayOnWithoutLock(int channel) {
        // turn relay on
        GpioPinDigitalOutput outputPin = pinStateHolder.getOutputPin(channelUID);
        PinState pinState = PinState.HIGH;
        GPIODataHolder.GPIO.setState(pinState, outputPin);
    }

    @Override
    public void turnRelayOff(int channel) {
        // request communication
        this.gateway.sendToSocket(this, () -> {
            this.turnRelayOffWithoutLock(channel);

            if (ledReflectsRelayStatus) {
                this.turnLedOff(channel);
            }
        });
    }

    private void turnRelayOffWithoutLock(int channel) {
        // turn relay off
        GpioPinDigitalOutput outputPin = pinStateHolder.getOutputPin(channelUID);
        PinState pinState = PinState.LOW;
        GPIODataHolder.GPIO.setState(pinState, outputPin);
    }

    @Override
    public void turnLedOn(int channel) {
        // request communication
        this.gateway.sendToSocket(this, () -> {
            this.turnLedOnWithoutLock(channel);
        });
    }

    private void turnLedOnWithoutLock(int channel) {
        // turn led on
        GpioPinDigitalOutput outputPin = pinStateHolder.getOutputPin(channel);
        PinState pinState = PinState.HIGH;
        GPIODataHolder.GPIO.setState(pinState, outputPin);
    }

    @Override
    public void turnLedOff(int channel) {
        // request communication
        this.gateway.sendToSocket(this, () -> {
            this.turnLedOffWithoutLock(channel);
        });
    }

    private void turnLedOffWithoutLock(int channel) {
        // turn led off
        GpioPinDigitalOutput outputPin = pinStateHolder.getOutputPin(channel);
        PinState pinState = PinState.LOW;
        GPIODataHolder.GPIO.setState(pinState, outputPin);
    }

    @Override
    public void isButtonPressed(int channel) {
        // !this->_tca->readPin(this->_id + TCA6424A_P10); //Active low so invert the result;
    }
}
