package com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.assign.primitive;

import com.blogspot.mydailyjava.bytebuddy.instrumentation.Instrumentation;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.StackManipulation;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.StackSize;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.assign.Assigner;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.type.TypeDescription;
import com.blogspot.mydailyjava.bytebuddy.utility.MockitoRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class VoidAwareAssignerVoidToNonVoidTest {

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {byte.class, Opcodes.ICONST_0},
                {short.class, Opcodes.ICONST_0},
                {char.class, Opcodes.ICONST_0},
                {int.class, Opcodes.ICONST_0},
                {long.class, Opcodes.LCONST_0},
                {float.class, Opcodes.FCONST_0},
                {double.class, Opcodes.DCONST_0},
                {Object.class, Opcodes.ACONST_NULL}
        });
    }

    private final Class<?> targetType;
    private final int opcode;

    public VoidAwareAssignerVoidToNonVoidTest(Class<?> targetType, int opcode) {
        this.targetType = targetType;
        this.opcode = opcode;
    }

    @Mock
    private TypeDescription sourceTypeDescription, targetTypeDescription;
    @Mock
    private Assigner chainedAssigner;
    @Mock
    private MethodVisitor methodVisitor;
    @Mock
    private Instrumentation.Context instrumentationContext;

    @Before
    public void setUp() throws Exception {
        when(sourceTypeDescription.represents(void.class)).thenReturn(true);
        when(sourceTypeDescription.isPrimitive()).thenReturn(true);
        when(targetTypeDescription.represents(targetType)).thenReturn(true);
        if (targetType.isPrimitive()) {
            when(targetTypeDescription.isPrimitive()).thenReturn(true);
        }
    }

    @After
    public void tearDown() throws Exception {
        verifyZeroInteractions(chainedAssigner);
        verifyZeroInteractions(instrumentationContext);
    }

    @Test
    public void testAssignDefaultValue() throws Exception {
        Assigner voidAwareAssigner = new VoidAwareAssigner(chainedAssigner, true);
        StackManipulation stackManipulation = voidAwareAssigner.assign(sourceTypeDescription, targetTypeDescription, false);
        assertThat(stackManipulation.isValid(), is(true));
        StackManipulation.Size size = stackManipulation.apply(methodVisitor, instrumentationContext);
        assertThat(size.getSizeImpact(), is(StackSize.of(targetType).getSize()));
        assertThat(size.getMaximalSize(), is(StackSize.of(targetType).getSize()));
        verify(methodVisitor).visitInsn(opcode);
        verifyNoMoreInteractions(methodVisitor);
    }

    @Test(expected = IllegalStateException.class)
    public void testAssignNoDefaultValue() throws Exception {
        Assigner voidAwareAssigner = new VoidAwareAssigner(chainedAssigner, false);
        StackManipulation stackManipulation = voidAwareAssigner.assign(sourceTypeDescription, targetTypeDescription, false);
        assertThat(stackManipulation.isValid(), is(false));
        stackManipulation.apply(methodVisitor, instrumentationContext);
    }
}