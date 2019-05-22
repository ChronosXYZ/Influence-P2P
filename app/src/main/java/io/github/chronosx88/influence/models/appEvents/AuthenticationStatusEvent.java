/*
 * Copyright (C) 2019 ChronosX88
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.chronosx88.influence.models.appEvents;

public class AuthenticationStatusEvent {
    public static final int NETWORK_ERROR = 0x0;
    public static final int INCORRECT_LOGIN_OR_PASSWORD = 0x1;
    public static final int CONNECT_AND_LOGIN_SUCCESSFUL = 0x2;

    public final int authenticationStatus;

    public AuthenticationStatusEvent(int authenticationStatus) {
        this.authenticationStatus = authenticationStatus;
    }
}
