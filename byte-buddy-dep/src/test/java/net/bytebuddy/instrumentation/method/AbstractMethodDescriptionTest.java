package net.bytebuddy.instrumentation.method;

import net.bytebuddy.instrumentation.attribute.annotation.AnnotationList;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.instrumentation.type.TypeList;
import net.bytebuddy.test.packaging.VisibilityMethodTestHelper;
import net.bytebuddy.test.utility.JavaVersionRule;
import net.bytebuddy.test.utility.PrecompiledTypeClassLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractMethodDescriptionTest {

    @Rule
    public MethodRule javaVersionRule = new JavaVersionRule();

    private Method firstMethod, secondMethod, thirdMethod;

    private Constructor<?> firstConstructor, secondConstructor;

    private ClassLoader classLoader;

    private static int hashCode(Method method) {
        int hashCode = new TypeDescription.ForLoadedType(method.getDeclaringClass()).hashCode();
        hashCode = 31 * hashCode + method.getName().hashCode();
        hashCode = 31 * hashCode + new TypeDescription.ForLoadedType(method.getReturnType()).hashCode();
        return 31 * hashCode + new TypeList.ForLoadedType(method.getParameterTypes()).hashCode();
    }

    private static int hashCode(Constructor<?> constructor) {
        int hashCode = new TypeDescription.ForLoadedType(constructor.getDeclaringClass()).hashCode();
        hashCode = 31 * hashCode + MethodDescription.CONSTRUCTOR_INTERNAL_NAME.hashCode();
        hashCode = 31 * hashCode + new TypeDescription.ForLoadedType(void.class).hashCode();
        return 31 * hashCode + new TypeList.ForLoadedType(constructor.getParameterTypes()).hashCode();
    }

    protected abstract MethodDescription describe(Method method);

    protected abstract MethodDescription describe(Constructor<?> constructor);

    @Before
    public void setUp() throws Exception {
        classLoader = new PrecompiledTypeClassLoader(getClass().getClassLoader());
        firstMethod = Sample.class.getDeclaredMethod("first");
        secondMethod = Sample.class.getDeclaredMethod("second", String.class, long.class);
        thirdMethod = Sample.class.getDeclaredMethod("third", Object[].class, int[].class);
        firstConstructor = Sample.class.getDeclaredConstructor(Void.class);
        secondConstructor = Sample.class.getDeclaredConstructor(int[].class, long.class);
    }

    @Test
    public void testPrecondition() throws Exception {
        assertThat(describe(firstMethod), not(equalTo(describe(secondMethod))));
        assertThat(describe(firstMethod), not(equalTo(describe(thirdMethod))));
        assertThat(describe(firstMethod), equalTo(describe(firstMethod)));
        assertThat(describe(secondMethod), equalTo(describe(secondMethod)));
        assertThat(describe(thirdMethod), equalTo(describe(thirdMethod)));
        assertThat(describe(firstMethod), is((MethodDescription) new MethodDescription.ForLoadedMethod(firstMethod)));
        assertThat(describe(secondMethod), is((MethodDescription) new MethodDescription.ForLoadedMethod(secondMethod)));
        assertThat(describe(thirdMethod), is((MethodDescription) new MethodDescription.ForLoadedMethod(thirdMethod)));
        assertThat(describe(firstConstructor), not(equalTo(describe(secondConstructor))));
        assertThat(describe(firstConstructor), equalTo(describe(firstConstructor)));
        assertThat(describe(secondConstructor), equalTo(describe(secondConstructor)));
        assertThat(describe(firstConstructor), is((MethodDescription) new MethodDescription.ForLoadedConstructor(firstConstructor)));
        assertThat(describe(secondConstructor), is((MethodDescription) new MethodDescription.ForLoadedConstructor(secondConstructor)));
    }

    @Test
    public void testReturnType() throws Exception {
        assertThat(describe(firstMethod).getReturnType(),
                is((TypeDescription) new TypeDescription.ForLoadedType(firstMethod.getReturnType())));
        assertThat(describe(secondMethod).getReturnType(),
                is((TypeDescription) new TypeDescription.ForLoadedType(secondMethod.getReturnType())));
        assertThat(describe(thirdMethod).getReturnType(),
                is((TypeDescription) new TypeDescription.ForLoadedType(thirdMethod.getReturnType())));
        assertThat(describe(firstConstructor).getReturnType(), is((TypeDescription) new TypeDescription.ForLoadedType(void.class)));
        assertThat(describe(secondConstructor).getReturnType(), is((TypeDescription) new TypeDescription.ForLoadedType(void.class)));
    }

    @Test
    public void testParameterTypes() throws Exception {
        assertThat(describe(firstMethod).getParameters().asTypeList(), is((TypeList) new TypeList.ForLoadedType(firstMethod.getParameterTypes())));
        assertThat(describe(secondMethod).getParameters().asTypeList(), is((TypeList) new TypeList.ForLoadedType(secondMethod.getParameterTypes())));
        assertThat(describe(thirdMethod).getParameters().asTypeList(), is((TypeList) new TypeList.ForLoadedType(thirdMethod.getParameterTypes())));
        assertThat(describe(firstConstructor).getParameters().asTypeList(), is((TypeList) new TypeList.ForLoadedType(firstConstructor.getParameterTypes())));
        assertThat(describe(secondConstructor).getParameters().asTypeList(), is((TypeList) new TypeList.ForLoadedType(secondConstructor.getParameterTypes())));
    }

    @Test
    public void testMethodName() throws Exception {
        assertThat(describe(firstMethod).getName(), is(firstMethod.getName()));
        assertThat(describe(secondMethod).getName(), is(secondMethod.getName()));
        assertThat(describe(thirdMethod).getName(), is(thirdMethod.getName()));
        assertThat(describe(firstConstructor).getName(), is(firstConstructor.getDeclaringClass().getName()));
        assertThat(describe(secondConstructor).getName(), is(secondConstructor.getDeclaringClass().getName()));
        assertThat(describe(firstMethod).getInternalName(), is(firstMethod.getName()));
        assertThat(describe(secondMethod).getInternalName(), is(secondMethod.getName()));
        assertThat(describe(thirdMethod).getInternalName(), is(thirdMethod.getName()));
        assertThat(describe(firstConstructor).getInternalName(), is(MethodDescription.CONSTRUCTOR_INTERNAL_NAME));
        assertThat(describe(secondConstructor).getInternalName(), is(MethodDescription.CONSTRUCTOR_INTERNAL_NAME));
    }

    @Test
    public void testDescriptor() throws Exception {
        assertThat(describe(firstMethod).getDescriptor(), is(Type.getMethodDescriptor(firstMethod)));
        assertThat(describe(secondMethod).getDescriptor(), is(Type.getMethodDescriptor(secondMethod)));
        assertThat(describe(thirdMethod).getDescriptor(), is(Type.getMethodDescriptor(thirdMethod)));
        assertThat(describe(firstConstructor).getDescriptor(), is(Type.getConstructorDescriptor(firstConstructor)));
        assertThat(describe(secondConstructor).getDescriptor(), is(Type.getConstructorDescriptor(secondConstructor)));
    }

    @Test
    public void testMethodModifiers() throws Exception {
        assertThat(describe(firstMethod).getModifiers(), is(firstMethod.getModifiers()));
        assertThat(describe(secondMethod).getModifiers(), is(secondMethod.getModifiers()));
        assertThat(describe(thirdMethod).getModifiers(), is(thirdMethod.getModifiers()));
        assertThat(describe(firstConstructor).getModifiers(), is(firstConstructor.getModifiers()));
        assertThat(describe(secondConstructor).getModifiers(), is(secondConstructor.getModifiers()));
    }

    @Test
    public void testMethodDeclaringType() throws Exception {
        assertThat(describe(firstMethod).getDeclaringType(), is((TypeDescription) new TypeDescription.ForLoadedType(firstMethod.getDeclaringClass())));
        assertThat(describe(secondMethod).getDeclaringType(), is((TypeDescription) new TypeDescription.ForLoadedType(secondMethod.getDeclaringClass())));
        assertThat(describe(thirdMethod).getDeclaringType(), is((TypeDescription) new TypeDescription.ForLoadedType(thirdMethod.getDeclaringClass())));
        assertThat(describe(firstConstructor).getDeclaringType(), is((TypeDescription) new TypeDescription.ForLoadedType(firstConstructor.getDeclaringClass())));
        assertThat(describe(secondConstructor).getDeclaringType(), is((TypeDescription) new TypeDescription.ForLoadedType(secondConstructor.getDeclaringClass())));
    }

    @Test
    public void testHashCode() throws Exception {
        assertThat(describe(firstMethod).hashCode(), is(hashCode(firstMethod)));
        assertThat(describe(secondMethod).hashCode(), is(hashCode(secondMethod)));
        assertThat(describe(thirdMethod).hashCode(), is(hashCode(thirdMethod)));
        assertThat(describe(firstMethod).hashCode(), not(is(hashCode(secondMethod))));
        assertThat(describe(firstMethod).hashCode(), not(is(hashCode(thirdMethod))));
        assertThat(describe(firstMethod).hashCode(), not(is(hashCode(firstConstructor))));
        assertThat(describe(firstMethod).hashCode(), not(is(hashCode(secondConstructor))));
        assertThat(describe(firstConstructor).hashCode(), is(hashCode(firstConstructor)));
        assertThat(describe(secondConstructor).hashCode(), is(hashCode(secondConstructor)));
        assertThat(describe(firstConstructor).hashCode(), not(is(hashCode(firstMethod))));
        assertThat(describe(firstConstructor).hashCode(), not(is(hashCode(secondMethod))));
        assertThat(describe(firstConstructor).hashCode(), not(is(hashCode(thirdMethod))));
        assertThat(describe(firstConstructor).hashCode(), not(is(hashCode(secondConstructor))));
    }

    @Test
    public void testEqualsMethod() throws Exception {
        MethodDescription identical = describe(firstMethod);
        assertThat(identical, equalTo(identical));
        assertThat(describe(firstMethod), equalTo(describe(firstMethod)));
        assertThat(describe(firstMethod), not(equalTo(describe(secondMethod))));
        assertThat(describe(firstMethod), not(equalTo(describe(thirdMethod))));
        assertThat(describe(firstMethod), not(equalTo(describe(firstConstructor))));
        assertThat(describe(firstMethod), not(equalTo(describe(secondConstructor))));
        assertThat(describe(firstMethod), equalTo((MethodDescription) new MethodDescription.ForLoadedMethod(firstMethod)));
        assertThat(describe(firstMethod), not(equalTo((MethodDescription) new MethodDescription.ForLoadedMethod(secondMethod))));
        assertThat(describe(firstMethod), not(equalTo((MethodDescription) new MethodDescription.ForLoadedMethod(thirdMethod))));
        assertThat(describe(firstMethod), not(equalTo((MethodDescription) new MethodDescription.ForLoadedConstructor(firstConstructor))));
        assertThat(describe(firstMethod), not(equalTo((MethodDescription) new MethodDescription.ForLoadedConstructor(secondConstructor))));
        MethodDescription equalMethod = mock(MethodDescription.class);
        when(equalMethod.getInternalName()).thenReturn(firstMethod.getName());
        when(equalMethod.getDeclaringType()).thenReturn(new TypeDescription.ForLoadedType(firstMethod.getDeclaringClass()));
        when(equalMethod.getReturnType()).thenReturn(new TypeDescription.ForLoadedType(firstMethod.getReturnType()));
        ParameterList equalMethodParameters = ParameterList.Explicit.latent(equalMethod, new TypeList.ForLoadedType(firstMethod.getParameterTypes()));
        when(equalMethod.getParameters()).thenReturn(equalMethodParameters);
        assertThat(describe(firstMethod), equalTo(equalMethod));
        MethodDescription equalMethodButName = mock(MethodDescription.class);
        when(equalMethodButName.getInternalName()).thenReturn(secondMethod.getName());
        when(equalMethodButName.getDeclaringType()).thenReturn(new TypeDescription.ForLoadedType(firstMethod.getDeclaringClass()));
        when(equalMethodButName.getReturnType()).thenReturn(new TypeDescription.ForLoadedType(firstMethod.getReturnType()));
        ParameterList equalMethodButNameParameters = ParameterList.Explicit.latent(equalMethodButName, new TypeList.ForLoadedType(firstMethod.getParameterTypes()));
        when(equalMethodButName.getParameters()).thenReturn(equalMethodButNameParameters);
        assertThat(describe(firstMethod), not(equalTo(equalMethodButName)));
        MethodDescription equalMethodButReturnType = mock(MethodDescription.class);
        when(equalMethodButReturnType.getInternalName()).thenReturn(firstMethod.getName());
        when(equalMethodButReturnType.getDeclaringType()).thenReturn(new TypeDescription.ForLoadedType(Object.class));
        when(equalMethodButReturnType.getReturnType()).thenReturn(new TypeDescription.ForLoadedType(firstMethod.getReturnType()));
        ParameterList equalMethodButReturnTypeParameters = ParameterList.Explicit.latent(equalMethodButReturnType, new TypeList.ForLoadedType(firstMethod.getParameterTypes()));
        when(equalMethodButReturnType.getParameters()).thenReturn(equalMethodButReturnTypeParameters);
        assertThat(describe(firstMethod), not(equalTo(equalMethodButReturnType)));
        MethodDescription equalMethodButDeclaringType = mock(MethodDescription.class);
        when(equalMethodButDeclaringType.getInternalName()).thenReturn(firstMethod.getName());
        when(equalMethodButDeclaringType.getDeclaringType()).thenReturn(new TypeDescription.ForLoadedType(firstMethod.getDeclaringClass()));
        when(equalMethodButDeclaringType.getReturnType()).thenReturn(new TypeDescription.ForLoadedType(secondMethod.getReturnType()));
        ParameterList equalMethodButDeclaringTypeParameters = ParameterList.Explicit.latent(equalMethodButDeclaringType, new TypeList.ForLoadedType(firstMethod.getParameterTypes()));
        when(equalMethodButDeclaringType.getParameters()).thenReturn(equalMethodButDeclaringTypeParameters);
        assertThat(describe(firstMethod), not(equalTo(equalMethodButDeclaringType)));
        MethodDescription equalMethodButParameterTypes = mock(MethodDescription.class);
        when(equalMethodButParameterTypes.getInternalName()).thenReturn(firstMethod.getName());
        when(equalMethodButParameterTypes.getDeclaringType()).thenReturn(new TypeDescription.ForLoadedType(firstMethod.getDeclaringClass()));
        when(equalMethodButParameterTypes.getReturnType()).thenReturn(new TypeDescription.ForLoadedType(firstMethod.getReturnType()));
        ParameterList equalMethodButParameterTypesParameters = ParameterList.Explicit.latent(equalMethodButParameterTypes, new TypeList.ForLoadedType(secondMethod.getParameterTypes()));
        when(equalMethodButParameterTypes.getParameters()).thenReturn(equalMethodButParameterTypesParameters);
        assertThat(describe(firstMethod), not(equalTo(equalMethodButParameterTypes)));
        assertThat(describe(firstMethod), not(equalTo(new Object())));
        assertThat(describe(firstMethod), not(equalTo(null)));
    }

    @Test
    public void testEqualsConstructor() throws Exception {
        MethodDescription identical = describe(firstConstructor);
        assertThat(identical, equalTo(identical));
        assertThat(describe(firstConstructor), equalTo(describe(firstConstructor)));
        assertThat(describe(firstConstructor), not(equalTo(describe(secondConstructor))));
        assertThat(describe(firstConstructor), not(equalTo(describe(firstMethod))));
        assertThat(describe(firstConstructor), not(equalTo(describe(secondMethod))));
        assertThat(describe(firstConstructor), not(equalTo(describe(thirdMethod))));
        assertThat(describe(firstConstructor), equalTo((MethodDescription) new MethodDescription.ForLoadedConstructor(firstConstructor)));
        assertThat(describe(firstConstructor), not(equalTo((MethodDescription) new MethodDescription.ForLoadedConstructor(secondConstructor))));
        assertThat(describe(firstConstructor), not(equalTo((MethodDescription) new MethodDescription.ForLoadedMethod(firstMethod))));
        assertThat(describe(firstConstructor), not(equalTo((MethodDescription) new MethodDescription.ForLoadedMethod(secondMethod))));
        assertThat(describe(firstConstructor), not(equalTo((MethodDescription) new MethodDescription.ForLoadedMethod(thirdMethod))));
        MethodDescription equalMethod = mock(MethodDescription.class);
        when(equalMethod.getInternalName()).thenReturn(MethodDescription.CONSTRUCTOR_INTERNAL_NAME);
        when(equalMethod.getDeclaringType()).thenReturn(new TypeDescription.ForLoadedType(firstConstructor.getDeclaringClass()));
        when(equalMethod.getReturnType()).thenReturn(new TypeDescription.ForLoadedType(void.class));
        ParameterList equalMethodParameters = ParameterList.Explicit.latent(equalMethod, new TypeList.ForLoadedType(firstConstructor.getParameterTypes()));
        when(equalMethod.getParameters()).thenReturn(equalMethodParameters);
        assertThat(describe(firstConstructor), equalTo(equalMethod));
        MethodDescription equalMethodButName = mock(MethodDescription.class);
        when(equalMethodButName.getInternalName()).thenReturn(firstMethod.getName());
        when(equalMethodButName.getDeclaringType()).thenReturn(new TypeDescription.ForLoadedType(firstConstructor.getDeclaringClass()));
        when(equalMethodButName.getReturnType()).thenReturn(new TypeDescription.ForLoadedType(void.class));
        ParameterList equalMethodButNameParameters = ParameterList.Explicit.latent(equalMethodButName, new TypeList.ForLoadedType(firstConstructor.getParameterTypes()));
        when(equalMethodButName.getParameters()).thenReturn(equalMethodButNameParameters);
        assertThat(describe(firstConstructor), not(equalTo(equalMethodButName)));
        MethodDescription equalMethodButReturnType = mock(MethodDescription.class);
        when(equalMethodButReturnType.getInternalName()).thenReturn(MethodDescription.CONSTRUCTOR_INTERNAL_NAME);
        when(equalMethodButReturnType.getDeclaringType()).thenReturn(new TypeDescription.ForLoadedType(Object.class));
        when(equalMethodButReturnType.getReturnType()).thenReturn(new TypeDescription.ForLoadedType(void.class));
        ParameterList equalMethodButReturnTypeParameters = ParameterList.Explicit.latent(equalMethodButReturnType, new TypeList.ForLoadedType(firstConstructor.getParameterTypes()));
        when(equalMethodButReturnType.getParameters()).thenReturn(equalMethodButReturnTypeParameters);
        assertThat(describe(firstConstructor), not(equalTo(equalMethodButReturnType)));
        MethodDescription equalMethodButDeclaringType = mock(MethodDescription.class);
        when(equalMethodButDeclaringType.getInternalName()).thenReturn(MethodDescription.CONSTRUCTOR_INTERNAL_NAME);
        when(equalMethodButDeclaringType.getDeclaringType()).thenReturn(new TypeDescription.ForLoadedType(firstConstructor.getDeclaringClass()));
        when(equalMethodButDeclaringType.getReturnType()).thenReturn(new TypeDescription.ForLoadedType(Object.class));
        ParameterList equalMethodButDeclaringTypeParameters = ParameterList.Explicit.latent(equalMethodButDeclaringType, new TypeList.ForLoadedType(firstConstructor.getParameterTypes()));
        when(equalMethodButDeclaringType.getParameters()).thenReturn(equalMethodButDeclaringTypeParameters);
        assertThat(describe(firstConstructor), not(equalTo(equalMethodButDeclaringType)));
        MethodDescription equalMethodButParameterTypes = mock(MethodDescription.class);
        when(equalMethodButParameterTypes.getInternalName()).thenReturn(MethodDescription.CONSTRUCTOR_INTERNAL_NAME);
        when(equalMethodButParameterTypes.getDeclaringType()).thenReturn(new TypeDescription.ForLoadedType(firstConstructor.getDeclaringClass()));
        when(equalMethodButParameterTypes.getReturnType()).thenReturn(new TypeDescription.ForLoadedType(void.class));
        ParameterList equalMethodButParameterTypesParameters = ParameterList.Explicit.latent(equalMethodButParameterTypes, new TypeList.ForLoadedType(secondConstructor.getParameterTypes()));
        when(equalMethodButParameterTypes.getParameters()).thenReturn(equalMethodButParameterTypesParameters);
        assertThat(describe(firstConstructor), not(equalTo(equalMethodButParameterTypes)));
        assertThat(describe(firstConstructor), not(equalTo(new Object())));
        assertThat(describe(firstConstructor), not(equalTo(null)));
    }

    @Test
    public void testToString() throws Exception {
        assertThat(describe(firstMethod).toString(), is(firstMethod.toString()));
        assertThat(describe(secondMethod).toString(), is(secondMethod.toString()));
        assertThat(describe(thirdMethod).toString(), is(thirdMethod.toString()));
        assertThat(describe(firstConstructor).toString(), is(firstConstructor.toString()));
        assertThat(describe(secondConstructor).toString(), is(secondConstructor.toString()));
    }

    @Test
    @JavaVersionRule.Enforce(8)
    public void testEqualsParameter() throws Exception {
        ParameterDescription identical = describe(secondMethod).getParameters().get(0);
        assertThat(identical, equalTo(identical));
        assertThat(identical, not(equalTo(new Object())));
        assertThat(identical, not(equalTo(null)));
        assertThat(describe(secondMethod).getParameters().get(0), is(describe(secondMethod).getParameters().get(0)));
        ParameterDescription equal = mock(ParameterDescription.class);
        when(equal.getDeclaringMethod()).thenReturn(describe(secondMethod));
        when(equal.getIndex()).thenReturn(0);
        assertThat(describe(secondMethod).getParameters().get(0), equalTo(equal));
        ParameterDescription notEqualMethod = mock(ParameterDescription.class);
        when(equal.getDeclaringMethod()).thenReturn(mock(MethodDescription.class));
        when(equal.getIndex()).thenReturn(0);
        assertThat(describe(secondMethod).getParameters().get(0), not(equalTo(notEqualMethod)));
        ParameterDescription notEqualMethodIndex = mock(ParameterDescription.class);
        when(equal.getDeclaringMethod()).thenReturn(describe(secondMethod));
        when(equal.getIndex()).thenReturn(1);
        assertThat(describe(secondMethod).getParameters().get(0), not(equalTo(notEqualMethodIndex)));
    }

    @Test
    @JavaVersionRule.Enforce(8)
    public void testHashCodeParameter() throws Exception {
        assertThat(describe(secondMethod).getParameters().get(0).hashCode(), is(hashCode(secondMethod, 0)));
        assertThat(describe(secondMethod).getParameters().get(1).hashCode(), is(hashCode(secondMethod, 1)));
        assertThat(describe(firstConstructor).getParameters().get(0).hashCode(), is(hashCode(firstConstructor, 0)));
    }

    private int hashCode(Method method, int index) {
        return hashCode(method) ^ index;
    }

    private int hashCode(Constructor<?> constructor, int index) {
        return hashCode(constructor) ^ index;
    }

    @Test
    @JavaVersionRule.Enforce(8)
    public void testToStringParameter() throws Exception {
        Class<?> executable = Class.forName("java.lang.reflect.Executable");
        Method method = executable.getDeclaredMethod("getParameters");
        assertThat(describe(secondMethod).getParameters().get(0).toString(), is(((Object[]) method.invoke(secondMethod))[0].toString()));
        assertThat(describe(secondMethod).getParameters().get(1).toString(), is(((Object[]) method.invoke(secondMethod))[1].toString()));
    }

    @Test
    @JavaVersionRule.Enforce(8)
    public void testParameterNameAndModifiers() throws Exception {
        Class<?> type = classLoader.loadClass("net.bytebuddy.test.precompiled.ParameterNames");
        assertThat(describe(type.getDeclaredMethod("foo", String.class, long.class, int.class)).getParameters().get(0).isNamed(), is(true));
        assertThat(describe(type.getDeclaredMethod("foo", String.class, long.class, int.class)).getParameters().get(1).isNamed(), is(true));
        assertThat(describe(type.getDeclaredMethod("foo", String.class, long.class, int.class)).getParameters().get(2).isNamed(), is(true));
        assertThat(describe(type.getDeclaredMethod("foo", String.class, long.class, int.class)).getParameters().get(0).getName(), is("first"));
        assertThat(describe(type.getDeclaredMethod("foo", String.class, long.class, int.class)).getParameters().get(1).getName(), is("second"));
        assertThat(describe(type.getDeclaredMethod("foo", String.class, long.class, int.class)).getParameters().get(2).getName(), is("third"));
        assertThat(describe(type.getDeclaredMethod("foo", String.class, long.class, int.class)).getParameters().get(0).getModifiers(), is(Opcodes.ACC_FINAL));
        assertThat(describe(type.getDeclaredMethod("foo", String.class, long.class, int.class)).getParameters().get(1).getModifiers(), is(0));
        assertThat(describe(type.getDeclaredMethod("foo", String.class, long.class, int.class)).getParameters().get(2).getModifiers(), is(0));
        assertThat(describe(type.getDeclaredMethod("bar", String.class, long.class, int.class)).getParameters().get(0).isNamed(), is(true));
        assertThat(describe(type.getDeclaredMethod("bar", String.class, long.class, int.class)).getParameters().get(1).isNamed(), is(true));
        assertThat(describe(type.getDeclaredMethod("bar", String.class, long.class, int.class)).getParameters().get(2).isNamed(), is(true));
        assertThat(describe(type.getDeclaredMethod("bar", String.class, long.class, int.class)).getParameters().get(0).getName(), is("first"));
        assertThat(describe(type.getDeclaredMethod("bar", String.class, long.class, int.class)).getParameters().get(1).getName(), is("second"));
        assertThat(describe(type.getDeclaredMethod("bar", String.class, long.class, int.class)).getParameters().get(2).getName(), is("third"));
        assertThat(describe(type.getDeclaredMethod("bar", String.class, long.class, int.class)).getParameters().get(0).getModifiers(), is(0));
        assertThat(describe(type.getDeclaredMethod("bar", String.class, long.class, int.class)).getParameters().get(1).getModifiers(), is(Opcodes.ACC_FINAL));
        assertThat(describe(type.getDeclaredMethod("bar", String.class, long.class, int.class)).getParameters().get(2).getModifiers(), is(0));
        assertThat(describe(type.getDeclaredConstructor(String.class, int.class)).getParameters().get(0).isNamed(), is(true));
        assertThat(describe(type.getDeclaredConstructor(String.class, int.class)).getParameters().get(1).isNamed(), is(true));
        assertThat(describe(type.getDeclaredConstructor(String.class, int.class)).getParameters().get(0).getName(), is("first"));
        assertThat(describe(type.getDeclaredConstructor(String.class, int.class)).getParameters().get(1).getName(), is("second"));
        assertThat(describe(type.getDeclaredConstructor(String.class, int.class)).getParameters().get(0).getModifiers(), is(0));
        assertThat(describe(type.getDeclaredConstructor(String.class, int.class)).getParameters().get(1).getModifiers(), is(Opcodes.ACC_FINAL));
    }

    @Test
    public void testNoParameterNameAndModifiers() throws Exception {
        assertThat(describe(secondMethod).getParameters().get(0).isNamed(), is(false));
        assertThat(describe(secondMethod).getParameters().get(1).isNamed(), is(false));
        assertThat(describe(secondMethod).getParameters().get(0).getName(), is("arg0"));
        assertThat(describe(secondMethod).getParameters().get(1).getName(), is("arg1"));
        assertThat(describe(secondMethod).getParameters().get(0).getModifiers(), is(0));
        assertThat(describe(secondMethod).getParameters().get(1).getModifiers(), is(0));
        assertThat(describe(firstConstructor).getParameters().get(0).isNamed(), is(canReadDebugInformation()));
        assertThat(describe(firstConstructor).getParameters().get(0).getName(), is(canReadDebugInformation() ? "argument" : "arg0"));
        assertThat(describe(firstConstructor).getParameters().get(0).getModifiers(), is(0));
    }

    protected abstract boolean canReadDebugInformation();

    @Test
    public void testSynthetic() throws Exception {
        assertThat(describe(firstMethod).isSynthetic(), is(firstMethod.isSynthetic()));
        assertThat(describe(secondMethod).isSynthetic(), is(secondMethod.isSynthetic()));
        assertThat(describe(thirdMethod).isSynthetic(), is(thirdMethod.isSynthetic()));
        assertThat(describe(firstConstructor).isSynthetic(), is(firstConstructor.isSynthetic()));
        assertThat(describe(secondConstructor).isSynthetic(), is(secondConstructor.isSynthetic()));
    }

    @Test
    public void testType() throws Exception {
        assertThat(describe(firstMethod).isMethod(), is(true));
        assertThat(describe(firstMethod).isConstructor(), is(false));
        assertThat(describe(firstMethod).isTypeInitializer(), is(false));
        assertThat(describe(firstConstructor).isMethod(), is(false));
        assertThat(describe(firstConstructor).isConstructor(), is(true));
        assertThat(describe(firstConstructor).isTypeInitializer(), is(false));
    }

    @Test
    public void testMethodIsVisibleTo() throws Exception {
        assertThat(describe(PublicType.class.getDeclaredMethod("publicMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(PublicType.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredMethod("protectedMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(PublicType.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredMethod("packagePrivateMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(PublicType.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredMethod("privateMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(PublicType.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredMethod("publicMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Sample.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredMethod("protectedMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Sample.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredMethod("packagePrivateMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Sample.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredMethod("privateMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Sample.class)), is(false));
        assertThat(describe(PublicType.class.getDeclaredMethod("publicMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredMethod("protectedMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PublicType.class.getDeclaredMethod("packagePrivateMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PublicType.class.getDeclaredMethod("privateMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PublicType.class.getDeclaredMethod("publicMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(VisibilityMethodTestHelper.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredMethod("protectedMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(VisibilityMethodTestHelper.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredMethod("packagePrivateMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(VisibilityMethodTestHelper.class)), is(false));
        assertThat(describe(PublicType.class.getDeclaredMethod("privateMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(VisibilityMethodTestHelper.class)), is(false));
        assertThat(describe(PackagePrivateType.class.getDeclaredMethod("publicMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PackagePrivateType.class.getDeclaredMethod("protectedMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PackagePrivateType.class.getDeclaredMethod("packagePrivateMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PackagePrivateType.class.getDeclaredMethod("privateMethod"))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
    }

    @Test
    public void testConstructorIsVisibleTo() throws Exception {
        assertThat(describe(PublicType.class.getDeclaredConstructor())
                .isVisibleTo(new TypeDescription.ForLoadedType(PublicType.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredConstructor(Void.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(PublicType.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredConstructor(Object.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(PublicType.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredConstructor(String.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(PublicType.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredConstructor())
                .isVisibleTo(new TypeDescription.ForLoadedType(Sample.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredConstructor(Void.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(Sample.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredConstructor(Object.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(Sample.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredConstructor(String.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(Sample.class)), is(false));
        assertThat(describe(PublicType.class.getDeclaredConstructor())
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredConstructor(Void.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PublicType.class.getDeclaredConstructor(Object.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PublicType.class.getDeclaredConstructor(String.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PublicType.class.getDeclaredConstructor())
                .isVisibleTo(new TypeDescription.ForLoadedType(VisibilityMethodTestHelper.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredConstructor(Void.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(VisibilityMethodTestHelper.class)), is(true));
        assertThat(describe(PublicType.class.getDeclaredConstructor(Object.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(VisibilityMethodTestHelper.class)), is(false));
        assertThat(describe(PublicType.class.getDeclaredConstructor(String.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(VisibilityMethodTestHelper.class)), is(false));
        assertThat(describe(PackagePrivateType.class.getDeclaredConstructor())
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PackagePrivateType.class.getDeclaredConstructor(Void.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PackagePrivateType.class.getDeclaredConstructor(Object.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(PackagePrivateType.class.getDeclaredConstructor(String.class))
                .isVisibleTo(new TypeDescription.ForLoadedType(Object.class)), is(false));
    }

    @Test
    public void testExceptions() throws Exception {
        assertThat(describe(firstMethod).getExceptionTypes(),
                is((TypeList) new TypeList.ForLoadedType(firstMethod.getExceptionTypes())));
        assertThat(describe(secondMethod).getExceptionTypes(),
                is((TypeList) new TypeList.ForLoadedType(secondMethod.getExceptionTypes())));
        assertThat(describe(thirdMethod).getExceptionTypes(),
                is((TypeList) new TypeList.ForLoadedType(thirdMethod.getExceptionTypes())));
        assertThat(describe(firstConstructor).getExceptionTypes(),
                is((TypeList) new TypeList.ForLoadedType(firstConstructor.getExceptionTypes())));
        assertThat(describe(secondConstructor).getExceptionTypes(),
                is((TypeList) new TypeList.ForLoadedType(secondConstructor.getExceptionTypes())));
    }

    @Test
    public void testAnnotations() throws Exception {
        assertThat(describe(firstMethod).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.Empty()));
        assertThat(describe(secondMethod).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.Empty()));
        assertThat(describe(thirdMethod).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.ForLoadedAnnotation(thirdMethod.getDeclaredAnnotations())));
        assertThat(describe(firstConstructor).getDeclaredAnnotations(), is((AnnotationList) new AnnotationList.Empty()));
        assertThat(describe(secondConstructor).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.ForLoadedAnnotation(secondConstructor.getDeclaredAnnotations())));
    }

    @Test
    public void testParameterAnnotations() throws Exception {
        assertThat(describe(secondMethod).getParameters().get(0).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.Empty()));
        assertThat(describe(secondMethod).getParameters().get(1).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.Empty()));
        assertThat(describe(thirdMethod).getParameters().get(0).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.ForLoadedAnnotation(thirdMethod.getParameterAnnotations()[0])));
        assertThat(describe(thirdMethod).getParameters().get(1).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.ForLoadedAnnotation(thirdMethod.getParameterAnnotations()[1])));
        assertThat(describe(firstConstructor).getParameters().get(0).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.Empty()));
        assertThat(describe(secondConstructor).getParameters().get(0).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.ForLoadedAnnotation(secondConstructor.getParameterAnnotations()[0])));
        assertThat(describe(secondConstructor).getParameters().get(1).getDeclaredAnnotations(),
                is((AnnotationList) new AnnotationList.ForLoadedAnnotation(secondConstructor.getParameterAnnotations()[1])));
    }

    @Test
    public void testRepresents() throws Exception {
        assertThat(describe(firstMethod).represents(firstMethod), is(true));
        assertThat(describe(firstMethod).represents(secondMethod), is(false));
        assertThat(describe(firstMethod).represents(thirdMethod), is(false));
        assertThat(describe(firstMethod).represents(firstConstructor), is(false));
        assertThat(describe(firstMethod).represents(secondConstructor), is(false));
        assertThat(describe(firstConstructor).represents(firstConstructor), is(true));
        assertThat(describe(firstConstructor).represents(secondConstructor), is(false));
        assertThat(describe(firstConstructor).represents(firstMethod), is(false));
        assertThat(describe(firstConstructor).represents(secondMethod), is(false));
        assertThat(describe(firstConstructor).represents(thirdMethod), is(false));
    }

    @Test
    public void testSpecializable() throws Exception {
        assertThat(describe(firstMethod).isSpecializableFor(new TypeDescription.ForLoadedType(Sample.class)), is(false));
        assertThat(describe(secondMethod).isSpecializableFor(new TypeDescription.ForLoadedType(Sample.class)), is(false));
        assertThat(describe(thirdMethod).isSpecializableFor(new TypeDescription.ForLoadedType(Sample.class)), is(true));
        assertThat(describe(thirdMethod).isSpecializableFor(new TypeDescription.ForLoadedType(SampleSub.class)), is(true));
        assertThat(describe(thirdMethod).isSpecializableFor(new TypeDescription.ForLoadedType(Object.class)), is(false));
        assertThat(describe(firstConstructor).isSpecializableFor(new TypeDescription.ForLoadedType(Sample.class)), is(true));
        assertThat(describe(firstConstructor).isSpecializableFor(new TypeDescription.ForLoadedType(SampleSub.class)), is(false));
    }

    @Test
    public void testInvokable() throws Exception {
        assertThat(describe(firstMethod).isInvokableOn(new TypeDescription.ForLoadedType(Sample.class)), is(false));
        assertThat(describe(secondMethod).isInvokableOn(new TypeDescription.ForLoadedType(Sample.class)), is(true));
        assertThat(describe(secondMethod).isInvokableOn(new TypeDescription.ForLoadedType(SampleSub.class)), is(true));
        assertThat(describe(secondMethod).isInvokableOn(new TypeDescription.ForLoadedType(Object.class)), is(false));
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface SampleAnnotation {

    }

    private static abstract class Sample {

        Sample(final Void argument) {

        }

        @SampleAnnotation
        private Sample(int[] first, @SampleAnnotation long second) throws IOException {

        }

        private static void first() {
            /* do nothing */
        }

        protected abstract Object second(String first, long second) throws RuntimeException, IOException;

        @SampleAnnotation
        public boolean[] third(@SampleAnnotation final Object[] first, int[] second) throws Throwable {
            return null;
        }
    }

    private static abstract class SampleSub extends Sample {

        protected SampleSub(Void argument) {
            super(argument);
        }
    }

    public static abstract class PublicType {

        public PublicType() {
            /* do nothing*/
        }

        protected PublicType(Void protectedConstructor) {
            /* do nothing*/
        }

        PublicType(Object packagePrivateConstructor) {
            /* do nothing*/
        }

        private PublicType(String privateConstructor) {
            /* do nothing*/
        }

        public abstract void publicMethod();

        protected abstract void protectedMethod();

        abstract void packagePrivateMethod();

        private void privateMethod() {
            /* do nothing*/
        }
    }

    static abstract class PackagePrivateType {

        public PackagePrivateType() {
            /* do nothing*/
        }

        protected PackagePrivateType(Void protectedConstructor) {
            /* do nothing*/
        }

        PackagePrivateType(Object packagePrivateConstructor) {
            /* do nothing*/
        }

        private PackagePrivateType(String privateConstructor) {
            /* do nothing*/
        }

        public abstract void publicMethod();

        protected abstract void protectedMethod();

        abstract void packagePrivateMethod();

        private void privateMethod() {
            /* do nothing*/
        }
    }
}
