/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema.loader.internal;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ReferenceResolverTest {

    @Parameters(name = "{0}")
    public static List<Object[]> params() {
        return asList(
                parList("fragment id", "http://x.y.z/root.json#foo", "http://x.y.z/root.json", "#foo"),
                parList("rel path", "http://example.org/foo", "http://example.org/bar", "foo"),
                parList("file name change", "http://x.y.z/schema/child.json",
                        "http://x.y.z/schema/parent.json",
                        "child.json"),
                parList("file name after folder path", "http://x.y.z/schema/child.json",
                        "http://x.y.z/schema/", "child.json"),
                parList("new root", "http://bserver.com", "http://aserver.com/",
                        "http://bserver.com"),
                parList("null parent", "http://a.b.c", null, "http://a.b.c"),
                parList("classpath single-slash",
                        "classpath:/hello/world.json/definitions/A",
                        "classpath:/hello/world.json/", "definitions/A"
                ),
                parList("classpath double-slash",
                        "classpath://hello/world.json#/definitions/A",
                        "classpath://hello/world.json", "#/definitions/A"
                ));
    }

    private static Object[] parList(String... params) {
        return params;
    }

    private final String expectedOutput;

    private final String parentScope;

    private final String encounteredSegment;

    public ReferenceResolverTest(String testcaseName, String expectedOutput,
            final String parentScope,
            final String encounteredSegment) {
        this.expectedOutput = expectedOutput;
        this.parentScope = parentScope;
        this.encounteredSegment = encounteredSegment;
    }

    @Test
    public void test() {
        String actual = ReferenceResolver.resolve(parentScope, encounteredSegment);
        assertEquals(expectedOutput, actual);
    }

    @Test
    public void testURI() {
        URI parentScopeURI;
        try {
            parentScopeURI = new URI(parentScope);
        } catch (URISyntaxException | NullPointerException e) {
            parentScopeURI = null;
        }
        URI actual = ReferenceResolver.resolve(parentScopeURI, encounteredSegment);
    }

    @Test
    public void resolveWrapsURISyntaxException() {
        try {
            ReferenceResolver.resolve("\\\\somethin\010g invalid///", "segment");
            fail("did not throw exception for invalid URI");
        } catch (RuntimeException e) {
            assertEquals(URISyntaxException.class, e.getCause().getClass());
        }
    }

    @Test public void resolveURIWrapsURISyntaxException() throws Exception {
        try {
            ReferenceResolver.resolve(new URI("http://example.com"), "\\\\somethin\010g invalid///");
            fail("did not throw exception for invalid URI");
        } catch (RuntimeException e) {
            assertEquals(URISyntaxException.class, e.getCause().getClass());
        }
    }

}
