
package cidrcalculator;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for HistoryProvider
 */
public final class CIDRHistory {
    public static final String AUTHORITY = "provider.CIDRHistory";

    // This class cannot be instantiated
    private CIDRHistory() {}
    
    /**
     * History table
     */
    public static final class History implements BaseColumns {
        // This class cannot be instantiated
        private History() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/history");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.lindanrandy.cursor.dir/vnd.cidrcalculator.history";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.lindanrandy.cursor.item/vnd.cidrcalculator.history";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        /**
         * The IP address itself
         * <P>Type: TEXT</P>
         */
        public static final String IP = "ip";

        /**
         * The number of bits
         * <P>Type: INTEGER</P>
         */
        public static final String BITS = "bits";

        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the note was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }
}
