package net.bytebuddy.instrumentation;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.instrumentation.method.MethodDescription;
import net.bytebuddy.instrumentation.method.MethodList;
import net.bytebuddy.instrumentation.method.bytecode.stack.StackManipulation;
import net.bytebuddy.instrumentation.method.bytecode.stack.assign.Assigner;
import net.bytebuddy.instrumentation.method.bytecode.stack.constant.TextConstant;
import net.bytebuddy.instrumentation.method.bytecode.stack.member.MethodReturn;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.test.utility.*;
import net.bytebuddy.utility.JavaInstance;
import net.bytebuddy.utility.JavaType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MethodCallTest extends AbstractInstrumentationTest {

    private static final String FOO = "foo", BAR = "bar";

    private static final String SINGLE_DEFAULT_METHOD = "net.bytebuddy.test.precompiled.SingleDefaultMethodInterface";

    @Rule
    public TestRule methodRule = new MockitoRule(this);

    @Rule
    public MethodRule javaVersionRule = new JavaVersionRule();

    @Mock
    private Assigner nonAssigner;

    private ClassLoader classLoader;

    @Before
    public void setUp() throws Exception {
        when(nonAssigner.assign(Mockito.any(TypeDescription.class), Mockito.any(TypeDescription.class), Mockito.anyBoolean()))
                .thenReturn(StackManipulation.Illegal.INSTANCE);
        classLoader = new PrecompiledTypeClassLoader(getClass().getClassLoader());
    }

    @Test
    public void testStaticMethodInvocationWithoutArguments() throws Exception {
        DynamicType.Loaded<SimpleMethod> loaded = instrument(SimpleMethod.class,
                MethodCall.invoke(SimpleMethod.class.getDeclaredMethod(BAR)),
                SimpleMethod.class.getClassLoader(),
                named(FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        SimpleMethod instance = loaded.getLoaded().newInstance();
        assertThat(instance.foo(), is(BAR));
        assertNotEquals(SimpleMethod.class, instance.getClass());
        assertThat(instance, instanceOf(SimpleMethod.class));
    }

    @Test
    public void testExternalStaticMethodInvocationWithoutArguments() throws Exception {
        DynamicType.Loaded<SimpleMethod> loaded = instrument(SimpleMethod.class,
                MethodCall.invoke(StaticExternalMethod.class.getDeclaredMethod(BAR)),
                SimpleMethod.class.getClassLoader(),
                named(FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        SimpleMethod instance = loaded.getLoaded().newInstance();
        assertThat(instance.foo(), is(BAR));
        assertNotEquals(SimpleMethod.class, instance.getClass());
        assertThat(instance, instanceOf(SimpleMethod.class));
    }

    @Test
    public void testInstanceMethodInvocationWithoutArguments() throws Exception {
        DynamicType.Loaded<InstanceMethod> loaded = instrument(InstanceMethod.class,
                MethodCall.invoke(InstanceMethod.class.getDeclaredMethod(BAR)),
                InstanceMethod.class.getClassLoader(),
                named(FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        InstanceMethod instance = loaded.getLoaded().newInstance();
        assertThat(instance.foo(), is(BAR));
        assertNotEquals(InstanceMethod.class, instance.getClass());
        assertThat(instance, instanceOf(InstanceMethod.class));
    }

    @Test
    public void testSuperConstructorInvocationWithoutArguments() throws Exception {
        DynamicType.Loaded<Object> loaded = instrument(Object.class,
                MethodCall.invoke(Object.class.getDeclaredConstructor()).onSuper(),
                Object.class.getClassLoader(),
                isConstructor());
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(0));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        Object instance = loaded.getLoaded().newInstance();
        assertNotEquals(Object.class, instance.getClass());
        assertThat(instance, instanceOf(Object.class));
    }

    @Test
    public void testObjectConstruction() throws Exception {
        DynamicType.Loaded<SelfReference> loaded = instrument(SelfReference.class,
                MethodCall.construct(SelfReference.class.getDeclaredConstructor()));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        SelfReference instance = loaded.getLoaded().newInstance();
        SelfReference created = instance.foo();
        assertEquals(SelfReference.class, created.getClass());
        assertNotEquals(SelfReference.class, instance.getClass());
        assertThat(instance, instanceOf(SelfReference.class));
        assertThat(created, not(instance));
    }

    @Test
    public void testSuperInvocation() throws Exception {
        DynamicType.Loaded<SuperMethodInvocation> loaded = instrument(SuperMethodInvocation.class,
                MethodCall.invokeSuper(),
                SuperMethodInvocation.class.getClassLoader(),
                takesArguments(0).and(named(FOO)));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        SuperMethodInvocation instance = loaded.getLoaded().newInstance();
        assertNotEquals(SuperMethodInvocation.class, instance.getClass());
        assertThat(instance, instanceOf(SuperMethodInvocation.class));
        assertThat(instance.foo(), is(FOO));
    }

    @Test
    public void testWithExplicitArgumentConstantPool() throws Exception {
        DynamicType.Loaded<MethodCallWithExplicitArgument> loaded = instrument(MethodCallWithExplicitArgument.class,
                MethodCall.invokeSuper().with(FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO, String.class), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        MethodCallWithExplicitArgument instance = loaded.getLoaded().newInstance();
        assertNotEquals(MethodCallWithExplicitArgument.class, instance.getClass());
        assertThat(instance, instanceOf(MethodCallWithExplicitArgument.class));
        assertThat(instance.foo(BAR), is(FOO));
    }

    @Test(expected = IllegalStateException.class)
    public void testWithExplicitArgumentConstantPoolNonAssignable() throws Exception {
        instrument(MethodCallWithExplicitArgument.class, MethodCall.invokeSuper()
                .with(FOO).withAssigner(nonAssigner, false));
    }

    @Test
    public void testWithExplicitArgumentField() throws Exception {
        DynamicType.Loaded<MethodCallWithExplicitArgument> loaded = instrument(MethodCallWithExplicitArgument.class,
                MethodCall.invokeSuper().withReference(FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO, String.class), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(1));
        MethodCallWithExplicitArgument instance = loaded.getLoaded().newInstance();
        assertNotEquals(MethodCallWithExplicitArgument.class, instance.getClass());
        assertThat(instance, instanceOf(MethodCallWithExplicitArgument.class));
        assertThat(instance.foo(BAR), is(FOO));
    }

    @Test(expected = IllegalStateException.class)
    public void testWithExplicitArgumentFieldNonAssignable() throws Exception {
        instrument(MethodCallWithExplicitArgument.class, MethodCall.invokeSuper()
                .withReference(FOO).withAssigner(nonAssigner, false));
    }

    @Test
    public void testWithParameter() throws Exception {
        DynamicType.Loaded<MethodCallWithExplicitArgument> loaded = instrument(MethodCallWithExplicitArgument.class,
                MethodCall.invokeSuper().withArgument(0));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO, String.class), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        MethodCallWithExplicitArgument instance = loaded.getLoaded().newInstance();
        assertNotEquals(MethodCallWithExplicitArgument.class, instance.getClass());
        assertThat(instance, instanceOf(MethodCallWithExplicitArgument.class));
        assertThat(instance.foo(BAR), is(BAR));
    }

    @Test
    public void testWithInstanceField() throws Exception {
        DynamicType.Loaded<MethodCallWithExplicitArgument> loaded = instrument(MethodCallWithExplicitArgument.class,
                MethodCall.invokeSuper().withInstanceField(String.class, FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO, String.class), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(1));
        MethodCallWithExplicitArgument instance = loaded.getLoaded().newInstance();
        Field field = instance.getClass().getDeclaredField(FOO);
        field.setAccessible(true);
        field.set(instance, FOO);
        assertNotEquals(MethodCallWithExplicitArgument.class, instance.getClass());
        assertThat(instance, instanceOf(MethodCallWithExplicitArgument.class));
        assertThat(instance.foo(BAR), is(FOO));
    }

    @Test(expected = IllegalStateException.class)
    public void testWithTooBigParameter() throws Exception {
        instrument(MethodCallWithExplicitArgument.class, MethodCall.invokeSuper().withArgument(1));
    }

    @Test(expected = IllegalStateException.class)
    public void testWithParameterNonAssignable() throws Exception {
        instrument(MethodCallWithExplicitArgument.class, MethodCall.invokeSuper()
                .withArgument(0).withAssigner(nonAssigner, false));
    }

    @Test
    public void testWithField() throws Exception {
        DynamicType.Loaded<MethodCallWithField> loaded = instrument(MethodCallWithField.class,
                MethodCall.invokeSuper().withField(FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO, String.class), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        MethodCallWithField instance = loaded.getLoaded().newInstance();
        instance.foo = FOO;
        assertNotEquals(MethodCallWithField.class, instance.getClass());
        assertThat(instance, instanceOf(MethodCallWithField.class));
        assertThat(instance.foo(BAR), is(FOO));
    }

    @Test(expected = IllegalStateException.class)
    public void testWithFieldNotExist() throws Exception {
        instrument(MethodCallWithField.class, MethodCall.invokeSuper().withField(BAR));
    }

    @Test(expected = IllegalStateException.class)
    public void testWithFieldNonAssignable() throws Exception {
        instrument(MethodCallWithField.class, MethodCall.invokeSuper().withField(FOO).withAssigner(nonAssigner, false));
    }

    @Test
    public void testWithFieldHierarchyVisibility() throws Exception {
        DynamicType.Loaded<InvisibleMethodCallWithField> loaded = instrument(InvisibleMethodCallWithField.class,
                MethodCall.invokeSuper().withField(FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO, String.class), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        InvisibleMethodCallWithField instance = loaded.getLoaded().newInstance();
        ((InvisibleBase) instance).foo = FOO;
        assertNotEquals(InvisibleMethodCallWithField.class, instance.getClass());
        assertThat(instance, instanceOf(InvisibleMethodCallWithField.class));
        assertThat(instance.foo(BAR), is(FOO));
    }

    @Test
    public void testWithThis() throws Exception {
        DynamicType.Loaded<MethodCallWithThis> loaded = instrument(MethodCallWithThis.class,
                MethodCall.invokeSuper().withThis());
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO, MethodCallWithThis.class), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        MethodCallWithThis instance = loaded.getLoaded().newInstance();
        assertNotEquals(MethodCallWithThis.class, instance.getClass());
        assertThat(instance, instanceOf(MethodCallWithThis.class));
        assertThat(instance.foo(null), is(instance));
    }

    @Test
    public void testInstrumentationAppendingMethod() throws Exception {
        DynamicType.Loaded<MethodCallAppending> loaded = instrument(MethodCallAppending.class,
                MethodCall.invokeSuper().andThen(new Instrumentation.Simple(new TextConstant(FOO), MethodReturn.REFERENCE)));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        MethodCallAppending instance = loaded.getLoaded().newInstance();
        assertNotEquals(MethodCallAppending.class, instance.getClass());
        assertThat(instance, instanceOf(MethodCallAppending.class));
        assertThat(instance.foo(), is((Object) FOO));
        instance.assertOnlyCall(FOO);
    }

    @Test
    public void testInstrumentationAppendingConstructor() throws Exception {
        DynamicType.Loaded<MethodCallAppending> loaded = instrument(MethodCallAppending.class,
                MethodCall.construct(Object.class.getDeclaredConstructor())
                        .andThen(new Instrumentation.Simple(new TextConstant(FOO), MethodReturn.REFERENCE)));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        MethodCallAppending instance = loaded.getLoaded().newInstance();
        assertNotEquals(MethodCallAppending.class, instance.getClass());
        assertThat(instance, instanceOf(MethodCallAppending.class));
        assertThat(instance.foo(), is((Object) FOO));
        instance.assertZeroCalls();
    }

    @Test
    public void testWithExplicitTarget() throws Exception {
        Object target = new Object();
        DynamicType.Loaded<ExplicitTarget> loaded = instrument(ExplicitTarget.class,
                MethodCall.invoke(Object.class.getDeclaredMethod("toString")).on(target));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(1));
        ExplicitTarget instance = loaded.getLoaded().newInstance();
        assertNotEquals(ExplicitTarget.class, instance.getClass());
        assertThat(instance, instanceOf(ExplicitTarget.class));
        assertThat(instance.foo(), is(target.toString()));
    }

    @Test
    public void testWithFieldTarget() throws Exception {
        Object target = new Object();
        DynamicType.Loaded<ExplicitTarget> loaded = instrument(ExplicitTarget.class,
                MethodCall.invoke(Object.class.getDeclaredMethod("toString")).onInstanceField(Object.class, FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredMethod(FOO), not(nullValue(Method.class)));
        assertThat(loaded.getLoaded().getDeclaredConstructors().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(1));
        ExplicitTarget instance = loaded.getLoaded().newInstance();
        Field field = loaded.getLoaded().getDeclaredField(FOO);
        field.setAccessible(true);
        field.set(instance, target);
        assertNotEquals(ExplicitTarget.class, instance.getClass());
        assertThat(instance, instanceOf(ExplicitTarget.class));
        assertThat(instance.foo(), is(target.toString()));
    }

    @Test
    public void testUnloadedType() throws Exception {
        DynamicType.Loaded<SimpleMethod> loaded = instrument(SimpleMethod.class,
                MethodCall.invoke(Foo.class.getDeclaredMethod(BAR, Object.class, Object.class))
                        .with(new TypeDescription.ForLoadedType(Object.class), new TypeDescription.ForLoadedType(String.class)),
                SimpleMethod.class.getClassLoader(),
                named(FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        SimpleMethod instance = loaded.getLoaded().newInstance();
        assertThat(instance.foo(), is("" + Object.class + String.class));
        assertNotEquals(SimpleMethod.class, instance.getClass());
        assertThat(instance, instanceOf(SimpleMethod.class));
    }

    @Test
    @JavaVersionRule.Enforce(7)
    public void testJava7Types() throws Exception {
        DynamicType.Loaded<SimpleMethod> loaded = instrument(SimpleMethod.class,
                MethodCall.invoke(Foo.class.getDeclaredMethod(BAR, Object.class, Object.class))
                        .with(makeMethodHandle(), makeMethodType(void.class)),
                SimpleMethod.class.getClassLoader(),
                named(FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        SimpleMethod instance = loaded.getLoaded().newInstance();
        assertThat(instance.foo(), is("" + makeMethodHandle() + makeMethodType(void.class)));
        assertNotEquals(SimpleMethod.class, instance.getClass());
        assertThat(instance, instanceOf(SimpleMethod.class));
    }

    @Test
    @JavaVersionRule.Enforce(7)
    public void testJava7TypesExplicit() throws Exception {
        DynamicType.Loaded<SimpleMethod> loaded = instrument(SimpleMethod.class,
                MethodCall.invoke(Foo.class.getDeclaredMethod(BAR, Object.class, Object.class))
                        .with(JavaInstance.MethodHandle.of(makeMethodHandle()), JavaInstance.MethodType.of(makeMethodType(void.class))),
                SimpleMethod.class.getClassLoader(),
                named(FOO));
        assertThat(loaded.getLoadedAuxiliaryTypes().size(), is(0));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        assertThat(loaded.getLoaded().getDeclaredFields().length, is(0));
        SimpleMethod instance = loaded.getLoaded().newInstance();
        assertThat(instance.foo(), is("" + makeMethodHandle() + makeMethodType(void.class)));
        assertNotEquals(SimpleMethod.class, instance.getClass());
        assertThat(instance, instanceOf(SimpleMethod.class));
    }

    @Test
    @JavaVersionRule.Enforce(8)
    public void testDefaultMethod() throws Exception {
        DynamicType.Loaded<?> loaded = instrument(Object.class,
                MethodCall.invoke(classLoader.loadClass(SINGLE_DEFAULT_METHOD).getDeclaredMethod(FOO)).onDefault(),
                classLoader,
                ElementMatchers.isMethod().and(ElementMatchers.not(isDeclaredBy(Object.class))),
                classLoader.loadClass(SINGLE_DEFAULT_METHOD));
        assertThat(loaded.getLoaded().getDeclaredMethods().length, is(1));
        Method method = loaded.getLoaded().getDeclaredMethod(FOO);
        Object instance = loaded.getLoaded().newInstance();
        assertThat(method.invoke(instance), is((Object) FOO));
    }

    @Test(expected = IllegalStateException.class)
    public void testDefaultMethodNotCompatible() throws Exception {
        instrument(Object.class, MethodCall.invoke(String.class.getDeclaredMethod("toString")).onDefault());
    }

    @Test(expected = IllegalStateException.class)
    public void testMethodTypeIncompatible() throws Exception {
        instrument(InstanceMethod.class,
                MethodCall.invoke(String.class.getDeclaredMethod("toLowerCase")),
                InstanceMethod.class.getClassLoader(),
                named(FOO));
    }

    @Test(expected = IllegalStateException.class)
    public void testArgumentIncompatibleTooFew() throws Exception {
        instrument(InstanceMethod.class,
                MethodCall.invoke(StaticIncompatibleExternalMethod.class.getDeclaredMethod("bar", String.class)),
                InstanceMethod.class.getClassLoader(),
                named(FOO));
    }

    @Test(expected = IllegalStateException.class)
    public void testArgumentIncompatibleTooMany() throws Exception {
        instrument(InstanceMethod.class,
                MethodCall.invoke(StaticIncompatibleExternalMethod.class.getDeclaredMethod("bar", String.class))
                        .with(FOO, BAR),
                InstanceMethod.class.getClassLoader(),
                named(FOO));
    }

    @Test(expected = IllegalStateException.class)
    public void testArgumentIncompatibleNotAssignable() throws Exception {
        instrument(InstanceMethod.class,
                MethodCall.invoke(StaticIncompatibleExternalMethod.class.getDeclaredMethod("bar", String.class))
                        .with(new Object()),
                InstanceMethod.class.getClassLoader(),
                named(FOO));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNonConstructorThrowsException() throws Exception {
        MethodCall.construct(mock(MethodDescription.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalIndex() throws Exception {
        MethodCall.invokeSuper().withArgument(-1);
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(MethodCall.class).apply();
        ObjectPropertyAssertion.of(MethodCall.WithoutSpecifiedTarget.class).apply();
        ObjectPropertyAssertion.of(MethodCall.Appender.class).apply();
        ObjectPropertyAssertion.of(MethodCall.MethodLocator.ForExplicitMethod.class).apply();
        ObjectPropertyAssertion.of(MethodCall.MethodLocator.ForInterceptedMethod.class).apply();
        ObjectPropertyAssertion.of(MethodCall.MethodInvoker.ForStandardInvocation.class).apply();
        ObjectPropertyAssertion.of(MethodCall.MethodInvoker.ForSuperMethodInvocation.class).apply();
        ObjectPropertyAssertion.of(MethodCall.MethodInvoker.ForDefaultMethodInvocation.class).apply();
        ObjectPropertyAssertion.of(MethodCall.TerminationHandler.ForChainedInvocation.class).apply();
        ObjectPropertyAssertion.of(MethodCall.TerminationHandler.ForMethodReturn.class).apply();
        ObjectPropertyAssertion.of(MethodCall.TargetHandler.ForStaticField.class).apply();
        ObjectPropertyAssertion.of(MethodCall.TargetHandler.ForInstanceField.class).apply();
        ObjectPropertyAssertion.of(MethodCall.TargetHandler.ForSelfOrStaticInvocation.class).apply();
        ObjectPropertyAssertion.of(MethodCall.TargetHandler.ForConstructingInvocation.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForNullConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForThisReference.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForBooleanConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForByteConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForCharacterConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForDoubleConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForStaticField.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForInstanceField.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForExistingField.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForFloatConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForIntegerConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForLongConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForMethodParameter.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForShortConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForTextConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForClassConstant.class).apply();
        ObjectPropertyAssertion.of(MethodCall.ArgumentLoader.ForJavaInstance.class).apply();
    }

    public static class SimpleMethod {

        public String foo() {
            return null;
        }

        public String bar() {
            return BAR;
        }
    }

    public static class StaticExternalMethod {

        public static String bar() {
            return BAR;
        }
    }

    public static class InstanceMethod {

        public String foo() {
            return null;
        }

        public String bar() {
            return BAR;
        }
    }

    public static class SelfReference {

        public SelfReference foo() {
            return null;
        }
    }

    public static class SuperMethodInvocation {

        public String foo() {
            return FOO;
        }
    }

    public static class MethodCallWithExplicitArgument {

        public String foo(String value) {
            return value;
        }
    }

    public static class MethodCallWithField {

        public String foo;

        public String foo(String value) {
            return value;
        }
    }

    public static class InvisibleMethodCallWithField extends InvisibleBase {

        private String foo;

        public String foo(String value) {
            return value;
        }
    }

    public static class InvisibleBase {

        public String foo;
    }

    public static class MethodCallWithThis {

        public MethodCallWithThis foo(MethodCallWithThis value) {
            return value;
        }
    }

    public static class MethodCallAppending extends CallTraceable {

        public Object foo() {
            register(FOO);
            return null;
        }
    }

    public static class ExplicitTarget {

        public String foo() {
            return null;
        }
    }

    public static class StaticIncompatibleExternalMethod {

        public static String bar(String value) {
            return null;
        }
    }

    private static Object makeMethodType(Class<?> returnType, Class<?>... parameterType) throws Exception {
        return JavaType.METHOD_TYPE.load().getDeclaredMethod("methodType", Class.class, Class[].class).invoke(null, returnType, parameterType);
    }

    public static class Foo {
        public static String bar(Object arg0, Object arg1) {
            return "" + arg0 + arg1;
        }
    }

    private static Object makeMethodHandle() throws Exception {
        Object lookup = Class.forName("java.lang.invoke.MethodHandles").getDeclaredMethod("publicLookup").invoke(null);
        return JavaType.METHOD_HANDLES_LOOKUP.load().getDeclaredMethod("findStatic", Class.class, String.class, JavaType.METHOD_TYPE.load())
                .invoke(lookup, Foo.class, BAR, makeMethodType(String.class, Object.class, Object.class));
    }
}
