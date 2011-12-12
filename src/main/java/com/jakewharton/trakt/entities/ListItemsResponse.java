package com.jakewharton.trakt.entities;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;

public class ListItemsResponse extends Response {
    private static final long serialVersionUID = 8123553856114248596L;

    public Integer inserted;
    @JsonProperty("already_exist") public Integer alreadyExist;
    public Integer skipped;
    @JsonProperty("skipped_array") public JsonNode skippedArray;

    /** @deprecated Use {@link #inserted} */
    @Deprecated
    public Integer getInserted() {
        return this.inserted;
    }
    /** @deprecated Use {@link #alreadyExist} */
    @Deprecated
    public Integer getAlreadyExist() {
        return this.alreadyExist;
    }
    /** @deprecated Use {@link #skipped} */
    @Deprecated
    public Integer getSkipped() {
        return this.skipped;
    }
    /** @deprecated Use {@link #skippedArray} */
    @Deprecated
    public JsonNode getSkippedArray() {
        return this.skippedArray;
    }
}
