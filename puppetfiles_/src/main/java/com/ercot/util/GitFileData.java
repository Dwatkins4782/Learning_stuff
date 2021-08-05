package com.ercot.util;

import java.util.Objects;

public class GitFileData {
    
    private final String content;
    private final String checksum;
    
    public GitFileData(final String cnt, final String check) {
        content = cnt;
        checksum = check;
    }

    public String getContent() {
        return content;
    }

    public String getChecksum() {
        return checksum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(checksum, content);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GitFileData)) {
            return false;
        }
        GitFileData other = (GitFileData) obj;
        return Objects.equals(checksum, other.checksum) && Objects.equals(content, other.content);
    }

    @Override
    public String toString() {
        return "GitFileData [getContent()=" + getContent() + ", getChecksum()=" + getChecksum() + ", hashCode()="
                + hashCode() + "]";
    }
}
