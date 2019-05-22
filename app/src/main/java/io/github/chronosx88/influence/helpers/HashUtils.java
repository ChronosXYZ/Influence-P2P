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

package io.github.chronosx88.influence.helpers;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public static String sha1(final String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("UTF-8"), 0, text.length());
            byte[] sha1hash = md.digest();
            return hashToString(sha1hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String hashToString(final byte[] buf) {
        if (buf == null) return "";
        int l = buf.length;
        StringBuffer result = new StringBuffer(2 * l);
        for (int i = 0; i < buf.length; i++) {
            appendByte(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX_PACK = "0123456789ABCDEF";

    private static void appendByte(final StringBuffer sb, final byte b) {
        sb
                .append(HEX_PACK.charAt((b >> 4) & 0x0f))
                .append(HEX_PACK.charAt(b & 0x0f));
    }
}
