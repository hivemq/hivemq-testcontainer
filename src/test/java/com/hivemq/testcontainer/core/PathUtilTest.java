package com.hivemq.testcontainer.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathUtilTest {

    @Test
    void test_empty_String() {
        assertEquals("/", PathUtil.preparePath(""));
    }

    @Test
    void test_only_delimiter() {
        assertEquals("/", PathUtil.preparePath("/"));
    }

    @Test
    void test_no_delimiter() {
        assertEquals("/path/", PathUtil.preparePath("path"));
    }

    @Test
    void test_delimiter_at_end() {
        assertEquals("/path/", PathUtil.preparePath("path/"));
    }

    @Test
    void test_delimiter_at_beginning() {
        assertEquals("/path/", PathUtil.preparePath("/path"));
    }
}