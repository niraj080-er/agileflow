package com.swiggy.agileflow.common.pagination;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Opaque cursor encoding for id-based (keyset) pagination. Cursors wrap the last
 * seen BIGINT id; results are ordered by ascending id, which is naturally
 * monotonic for our identity primary keys.
 */
public final class Cursors {

    private Cursors() {
    }

    /** Encodes the last-seen id into an opaque cursor token. */
    public static String encode(long lastId) {
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(("id:" + lastId).getBytes(StandardCharsets.UTF_8));
    }

    /** Decodes a cursor token into the last-seen id, or returns 0 when {@code cursor} is null/blank. */
    public static long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0L;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!decoded.startsWith("id:")) {
                throw new IllegalArgumentException("Unrecognized cursor");
            }
            return Long.parseLong(decoded.substring(3));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cursor: " + cursor);
        }
    }
}
