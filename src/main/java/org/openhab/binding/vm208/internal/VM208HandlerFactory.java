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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VM208HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.vm208", service = ThingHandlerFactory.class)
public class VM208HandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(VM208HandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_VM208INT, THING_TYPE_VM208EX);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        logger.debug("Trying to create handler for {}", thingTypeUID.getAsString());
        if (THING_TYPE_VM208EX.equals(thingTypeUID)) {
            return new VM208ExHandler(thing);
        } else if (THING_TYPE_VM208INT.equals(thingTypeUID)) {
            return new VM208IntHandler((Bridge) thing);
        }
        logger.debug("No handler match for {}", thingTypeUID.getAsString());

        return null;
    }
}
