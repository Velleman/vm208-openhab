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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VM208BaseHandler} class defines common constants, which are
 * used across every VM208 module.
 *
 * @author Simon Lamon - Initial contribution
 */
@NonNullByDefault()
public interface VM208BaseHandler {

    public int getSocket();
}
