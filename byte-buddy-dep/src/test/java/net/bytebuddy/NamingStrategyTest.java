package net.bytebuddy;

import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.test.utility.MockitoRule;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.mockito.Mockito.*;

public class NamingStrategyTest {

    private static final String FOO = "foo", BAR = "bar", JAVA_QUX = "java.qux";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private NamingStrategy.UnnamedType unnamedType;

    @Mock
    private NamingStrategy.SuffixingRandom.BaseNameResolver baseNameResolver;

    @Mock
    private TypeDescription typeDescription;

    @Test
    public void testSuffixingRandomNonConflictingPackage() throws Exception {
        when(unnamedType.getSuperClass()).thenReturn(typeDescription);
        when(typeDescription.getName()).thenReturn(FOO);
        NamingStrategy namingStrategy = new NamingStrategy.SuffixingRandom(BAR);
        assertThat(namingStrategy.name(unnamedType), startsWith(FOO + "$" + BAR + "$"));
        verify(unnamedType, atLeast(1)).getSuperClass();
        verifyNoMoreInteractions(unnamedType);
    }

    @Test
    public void testSuffixingRandomConflictingPackage() throws Exception {
        when(baseNameResolver.resolve(unnamedType)).thenReturn(JAVA_QUX);
        NamingStrategy namingStrategy = new NamingStrategy.SuffixingRandom(FOO, baseNameResolver, BAR);
        assertThat(namingStrategy.name(unnamedType), startsWith(BAR + "." + JAVA_QUX + "$" + FOO + "$"));
        verifyZeroInteractions(unnamedType);
        verify(baseNameResolver).resolve(unnamedType);
        verifyNoMoreInteractions(baseNameResolver);
    }

    @Test
    public void testBaseNameResolvers() throws Exception {
        assertThat(new NamingStrategy.SuffixingRandom.BaseNameResolver.ForFixedValue(FOO).resolve(unnamedType), is(FOO));
        when(typeDescription.getName()).thenReturn(FOO);
        assertThat(new NamingStrategy.SuffixingRandom.BaseNameResolver.ForGivenType(typeDescription).resolve(unnamedType), is(FOO));
        when(unnamedType.getSuperClass()).thenReturn(typeDescription);
        assertThat(NamingStrategy.SuffixingRandom.BaseNameResolver.ForUnnamedType.INSTANCE.resolve(unnamedType), is(FOO));
    }

    @Test
    public void testSuffixingRandomObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(NamingStrategy.SuffixingRandom.class).apply();
        ObjectPropertyAssertion.of(NamingStrategy.SuffixingRandom.BaseNameResolver.ForGivenType.class).apply();
        ObjectPropertyAssertion.of(NamingStrategy.SuffixingRandom.BaseNameResolver.ForUnnamedType.class).apply();
        ObjectPropertyAssertion.of(NamingStrategy.SuffixingRandom.BaseNameResolver.ForFixedValue.class).apply();
    }

    @Test
    public void testFixed() throws Exception {
        NamingStrategy namingStrategy = new NamingStrategy.Fixed(FOO);
        assertThat(namingStrategy.name(unnamedType), is(FOO));
        verifyZeroInteractions(unnamedType);
    }

    @Test
    public void testFixedObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(NamingStrategy.Fixed.class).apply();
    }

    @Test
    public void testPrefixingRandom() throws Exception {
        when(unnamedType.getSuperClass()).thenReturn(TypeDescription.OBJECT);
        NamingStrategy namingStrategy = new NamingStrategy.PrefixingRandom(FOO);
        assertThat(namingStrategy.name(unnamedType), startsWith(FOO + "." + Object.class.getName()));
        verify(unnamedType).getSuperClass();
        verifyNoMoreInteractions(unnamedType);
    }

    @Test
    public void testPrefixingRandomEqualsHashCode() throws Exception {
        ObjectPropertyAssertion.of(NamingStrategy.PrefixingRandom.class).apply();
    }
}
