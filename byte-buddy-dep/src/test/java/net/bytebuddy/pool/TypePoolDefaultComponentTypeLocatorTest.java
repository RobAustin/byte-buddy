package net.bytebuddy.pool;

import net.bytebuddy.instrumentation.method.MethodDescription;
import net.bytebuddy.instrumentation.method.MethodList;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import net.bytebuddy.utility.RandomString;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TypePoolDefaultComponentTypeLocatorTest {

    private static final String FOO = "foo", BAR = "bar", BAR_DESCRIPTOR = "L" + BAR + ";", QUX = "qux", BAZ = "baz";

    @Test(expected = IllegalStateException.class)
    public void testIllegal() throws Exception {
        TypePool.Default.ComponentTypeLocator.Illegal.INSTANCE.bind(FOO);
    }

    @Test
    public void testForAnnotationProperty() throws Exception {
        TypePool typePool = mock(TypePool.class);
        TypeDescription typeDescription = mock(TypeDescription.class);
        when(typePool.describe(BAR)).thenReturn(new TypePool.Resolution.Simple(typeDescription));
        MethodDescription methodDescription = mock(MethodDescription.class);
        when(typeDescription.getDeclaredMethods()).thenReturn(new MethodList.Explicit(Collections.singletonList(methodDescription)));
        when(methodDescription.getSourceCodeName()).thenReturn(FOO);
        TypeDescription returnType = mock(TypeDescription.class);
        when(methodDescription.getReturnType()).thenReturn(returnType);
        TypeDescription componentType = mock(TypeDescription.class);
        when(returnType.getComponentType()).thenReturn(componentType);
        when(componentType.getName()).thenReturn(QUX);
        assertThat(new TypePool.Default.ComponentTypeLocator.ForAnnotationProperty(typePool, BAR_DESCRIPTOR)
                .bind(FOO).lookup(), is(QUX));
    }

    @Test
    public void testForArrayType() throws Exception {
        assertThat(new TypePool.Default.ComponentTypeLocator.ForArrayType("()[" + BAR_DESCRIPTOR).bind(FOO).lookup(), is(BAR));
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(TypePool.Default.ComponentTypeLocator.ForAnnotationProperty.class).apply();
        ObjectPropertyAssertion.of(TypePool.Default.ComponentTypeLocator.ForAnnotationProperty.Bound.class).skipSynthetic().apply();
        ObjectPropertyAssertion.of(TypePool.Default.ComponentTypeLocator.ForArrayType.class).create(new ObjectPropertyAssertion.Creator<String>() {
            @Override
            public String create() {
                return "()L" + RandomString.make() + ";";
            }
        }).apply();
        ObjectPropertyAssertion.of(TypePool.Default.ComponentTypeLocator.Illegal.class).apply();
        ObjectPropertyAssertion.of(TypePool.Default.TypeExtractor.OnTypeCollector.class).applyMutable();
        ObjectPropertyAssertion.of(TypePool.Default.TypeExtractor.MethodExtractor.class).create(new ObjectPropertyAssertion.Creator<String>() {
            @Override
            public String create() {
                return "(LFoo;)LBar;";
            }
        }).applyMutable();
        ObjectPropertyAssertion.of(TypePool.Default.ParameterBag.class).applyMutable();
        ObjectPropertyAssertion.of(TypePool.Default.TypeExtractor.MethodExtractor.OnMethodCollector.class).applyMutable();
        ObjectPropertyAssertion.of(TypePool.Default.TypeExtractor.MethodExtractor.OnMethodParameterCollector.class).applyMutable();
        ObjectPropertyAssertion.of(TypePool.Default.TypeExtractor.FieldExtractor.class).applyMutable();
        ObjectPropertyAssertion.of(TypePool.Default.TypeExtractor.FieldExtractor.OnFieldCollector.class).applyMutable();
        ObjectPropertyAssertion.of(TypePool.Default.TypeExtractor.AnnotationExtractor.class).applyMutable();
        ObjectPropertyAssertion.of(TypePool.Default.TypeExtractor.AnnotationExtractor.ArrayLookup.class).applyMutable();
        ObjectPropertyAssertion.of(TypePool.Default.TypeExtractor.AnnotationExtractor.AnnotationLookup.class).applyMutable();
    }
}
