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
package org.openhab.binding.vm208.internal.i2c;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

/**
 * The {@link PinStateHolder} is a class where MCP23017 PIN state is held
 *
 * @author Anatol Ogorek - Initial contribution
 */
public class PinStateHolder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<ChannelUID, GpioPinDigitalInput> inputPins = new HashMap<>();
    private Map<ChannelUID, GpioPinDigitalOutput> outputPins = new HashMap<>();
    private TCA6424AGpioProvider tcaProvider;
    private Thing thing;

    public PinStateHolder(TCA6424AGpioProvider tcaProvider, Thing thing) {
        this.tcaProvider = tcaProvider;
        this.thing = thing;
    }

    public GpioPin getInputPin(ChannelUID channel) {
        return inputPins.get(channel);
    }

    public GpioPinDigitalOutput getOutputPin(Pin pin) {
        logger.debug("Getting output pin for channel {}", channel);
        GpioPinDigitalOutput outputPin = outputPins.get(channel);
        if (outputPin == null) {
            outputPin = initializeOutputPin(channel);
            outputPins.put(channel, outputPin);
        }
        return outputPin;
    }

    private GpioPinDigitalOutput initializeOutputPin(Pin pin) {
        PinState pinState = PinState.LOW;
        logger.debug("initializing for pinState {}", pinState);
        GpioPinDigitalOutput gpioPin = GPIODataHolder.GPIO.provisionDigitalOutputPin(tcaProvider, pin,
                channel.getIdWithoutGroup(), pinState);
        logger.debug("Bound digital output for PIN: {}, channel: {}, pinState: {}", pin, channel, pinState);
        return gpioPin;
    }

    public void unBindGpioPins() {
        inputPins.values().stream().forEach(gpioPin -> GPIODataHolder.GPIO.unprovisionPin(gpioPin));
        inputPins.clear();

        outputPins.values().stream().forEach(gpioPin -> GPIODataHolder.GPIO.unprovisionPin(gpioPin));
        outputPins.clear();
    }

    public ChannelUID getChannelForInputPin(GpioPinDigitalInput pin) {
        Optional<Entry<ChannelUID, GpioPinDigitalInput>> result = inputPins.entrySet().stream()
                .filter(entry -> entry.getValue().equals(pin)).findFirst();
        if (result.isPresent()) {
            return result.get().getKey();
        }
        return null;
    }

    public void addInputPin(GpioPinDigitalInput pin, ChannelUID channel) {
        inputPins.put(channel, pin);
    }
}
