/*
 * Copyright 2020 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.testcontainer.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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