package net.bytebuddy.instrumentation.field;

import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.test.utility.MockitoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FieldListExplicitTest {

    private static final String FOO = "foo", BAR = "bar";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private FieldDescription firstFieldDescription, secondFieldDescription;

    private FieldList fieldList;

    @Before
    public void setUp() throws Exception {
        fieldList = new FieldList.Explicit(Arrays.asList(firstFieldDescription, secondFieldDescription));
    }

    @Test
    public void testFieldList() throws Exception {
        assertThat(fieldList.size(), is(2));
        assertThat(fieldList.get(0), is(firstFieldDescription));
        assertThat(fieldList.get(1), is(secondFieldDescription));
    }

    @Test
    public void testMethodListFilter() throws Exception {
        @SuppressWarnings("unchecked")
        ElementMatcher<? super FieldDescription> fieldMatcher = mock(ElementMatcher.class);
        when(fieldMatcher.matches(firstFieldDescription)).thenReturn(true);
        fieldList = fieldList.filter(fieldMatcher);
        assertThat(fieldList.size(), is(1));
        assertThat(fieldList.getOnly(), is(firstFieldDescription));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetOnly() throws Exception {
        fieldList.getOnly();
    }

    @Test
    public void testSubList() throws Exception {
        assertThat(fieldList.subList(0, 1), is((FieldList) new FieldList.Explicit(Collections.singletonList(firstFieldDescription))));
    }
}
