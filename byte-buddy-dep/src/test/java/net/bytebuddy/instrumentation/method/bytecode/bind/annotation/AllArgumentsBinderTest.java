package net.bytebuddy.instrumentation.method.bytecode.bind.annotation;

import net.bytebuddy.instrumentation.Instrumentation;
import net.bytebuddy.instrumentation.attribute.annotation.AnnotationList;
import net.bytebuddy.instrumentation.method.bytecode.bind.MethodDelegationBinder;
import net.bytebuddy.instrumentation.method.bytecode.stack.StackManipulation;
import net.bytebuddy.instrumentation.method.bytecode.stack.StackSize;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class AllArgumentsBinderTest extends AbstractAnnotationBinderTest<AllArguments> {

    private static final String FOO = "foo";

    @Mock
    private TypeDescription firstSourceType, secondSourceType;

    @Mock
    private TypeDescription targetType, componentType;

    public AllArgumentsBinderTest() {
        super(AllArguments.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(firstSourceType.getStackSize()).thenReturn(StackSize.SINGLE);
        when(secondSourceType.getStackSize()).thenReturn(StackSize.SINGLE);
    }

    @Override
    protected TargetMethodAnnotationDrivenBinder.ParameterBinder<AllArguments> getSimpleBinder() {
        return AllArguments.Binder.INSTANCE;
    }

    @Test
    public void testLegalStrictBindingRuntimeType() throws Exception {
        when(target.getIndex()).thenReturn(1);
        testLegalStrictBinding(false);
    }

    @Test
    public void testLegalStrictBindingNoRuntimeType() throws Exception {
        when(target.getIndex()).thenReturn(1);
        RuntimeType runtimeType = mock(RuntimeType.class);
        doReturn(RuntimeType.class).when(runtimeType).annotationType();
        when(target.getDeclaredAnnotations()).thenReturn(new AnnotationList.ForLoadedAnnotation(runtimeType));
        testLegalStrictBinding(true, runtimeType);
    }

    private void testLegalStrictBinding(boolean dynamicallyTyped, Annotation... targetAnnotation) throws Exception {
        when(annotation.value()).thenReturn(AllArguments.Assignment.STRICT);
        when(stackManipulation.isValid()).thenReturn(true);
        when(sourceTypeList.iterator()).thenReturn(Arrays.asList(firstSourceType, secondSourceType).iterator());
        when(source.isStatic()).thenReturn(false);
        when(targetType.isArray()).thenReturn(true);
        when(targetType.getComponentType()).thenReturn(componentType);
        when(componentType.getStackSize()).thenReturn(StackSize.SINGLE);
        when(target.getTypeDescription()).thenReturn(targetType);
        when(target.getDeclaredAnnotations()).thenReturn(new AnnotationList.ForLoadedAnnotation(targetAnnotation));
        MethodDelegationBinder.ParameterBinding<?> parameterBinding = AllArguments.Binder.INSTANCE
                .bind(annotationDescription, source, target, instrumentationTarget, assigner);
        assertThat(parameterBinding.isValid(), is(true));
        verify(source, atLeast(1)).getParameters();
        verify(source, atLeast(1)).isStatic();
        verify(target, atLeast(1)).getTypeDescription();
        verify(target, atLeast(1)).getDeclaredAnnotations();
        verify(assigner).assign(firstSourceType, componentType, dynamicallyTyped);
        verify(assigner).assign(secondSourceType, componentType, dynamicallyTyped);
        verifyNoMoreInteractions(assigner);
    }

    @Test
    public void testIllegalBinding() throws Exception {
        when(target.getIndex()).thenReturn(1);
        when(annotation.value()).thenReturn(AllArguments.Assignment.STRICT);
        when(stackManipulation.isValid()).thenReturn(false);
        when(sourceTypeList.iterator()).thenReturn(Arrays.asList(firstSourceType, secondSourceType).iterator());
        when(source.isStatic()).thenReturn(false);
        when(targetType.isArray()).thenReturn(true);
        when(targetType.getComponentType()).thenReturn(componentType);
        when(componentType.getStackSize()).thenReturn(StackSize.SINGLE);
        when(target.getTypeDescription()).thenReturn(targetType);
        when(target.getDeclaredAnnotations()).thenReturn(new AnnotationList.Empty());
        MethodDelegationBinder.ParameterBinding<?> parameterBinding = AllArguments.Binder.INSTANCE
                .bind(annotationDescription, source, target, instrumentationTarget, assigner);
        assertThat(parameterBinding.isValid(), is(false));
        verify(source, atLeast(1)).getParameters();
        verify(source, atLeast(1)).isStatic();
        verify(target, atLeast(1)).getTypeDescription();
        verify(target, atLeast(1)).getDeclaredAnnotations();
        verify(assigner).assign(firstSourceType, componentType, false);
        verifyNoMoreInteractions(assigner);
    }

    @Test
    public void testLegalSlackBinding() throws Exception {
        when(target.getIndex()).thenReturn(1);
        when(annotation.value()).thenReturn(AllArguments.Assignment.SLACK);
        when(stackManipulation.isValid()).thenReturn(false);
        when(sourceTypeList.iterator()).thenReturn(Arrays.asList(firstSourceType, secondSourceType).iterator());
        when(source.isStatic()).thenReturn(false);
        when(targetType.isArray()).thenReturn(true);
        when(targetType.getComponentType()).thenReturn(componentType);
        when(componentType.getStackSize()).thenReturn(StackSize.SINGLE);
        when(target.getTypeDescription()).thenReturn(targetType);
        when(target.getDeclaredAnnotations()).thenReturn(new AnnotationList.Empty());
        when(componentType.getInternalName()).thenReturn(FOO);
        MethodDelegationBinder.ParameterBinding<?> parameterBinding = AllArguments.Binder.INSTANCE
                .bind(annotationDescription, source, target, instrumentationTarget, assigner);
        MethodVisitor methodVisitor = mock(MethodVisitor.class);
        Instrumentation.Context instrumentationContext = mock(Instrumentation.Context.class);
        StackManipulation.Size size = parameterBinding.apply(methodVisitor, instrumentationContext);
        assertThat(size.getSizeImpact(), is(1));
        assertThat(size.getMaximalSize(), is(1));
        verify(methodVisitor).visitInsn(Opcodes.ICONST_0);
        verify(methodVisitor).visitTypeInsn(Opcodes.ANEWARRAY, FOO);
        verifyNoMoreInteractions(methodVisitor);
        verifyZeroInteractions(instrumentationContext);
        assertThat(parameterBinding.isValid(), is(true));
        verify(source, atLeast(1)).getParameters();
        verify(source, atLeast(1)).isStatic();
        verify(target, atLeast(1)).getTypeDescription();
        verify(target, atLeast(1)).getDeclaredAnnotations();
        verify(assigner).assign(firstSourceType, componentType, false);
        verify(assigner).assign(secondSourceType, componentType, false);
        verifyNoMoreInteractions(assigner);
    }

    @Test(expected = IllegalStateException.class)
    public void testNonArrayTypeBinding() throws Exception {
        when(target.getIndex()).thenReturn(0);
        TypeDescription targetType = mock(TypeDescription.class);
        when(targetType.isArray()).thenReturn(false);
        when(target.getTypeDescription()).thenReturn(targetType);
        AllArguments.Binder.INSTANCE.bind(annotationDescription, source, target, instrumentationTarget, assigner);
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(AllArguments.Assignment.class).apply();
        ObjectPropertyAssertion.of(AllArguments.Binder.class).apply();
    }
}
