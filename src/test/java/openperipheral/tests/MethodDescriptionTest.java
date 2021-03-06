package openperipheral.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import openperipheral.adapter.method.MethodDeclaration;
import openperipheral.api.adapter.method.Alias;
import openperipheral.api.adapter.method.Arg;
import openperipheral.api.adapter.method.Env;
import openperipheral.api.adapter.method.IMultiReturn;
import openperipheral.api.adapter.method.MultipleReturn;
import openperipheral.api.adapter.method.Optionals;
import openperipheral.api.adapter.method.ReturnType;
import openperipheral.api.adapter.method.ScriptCallable;
import openperipheral.api.helpers.MultiReturn;
import org.junit.Assert;
import org.junit.Test;

public class MethodDescriptionTest {

	private static final ImmutableMap<String, Class<?>> NO_OPTIONALS = ImmutableMap.<String, Class<?>> of();

	public static class A {}

	public static class B extends A {}

	public static class C {}

	public static class D {
		public String test() {
			return "out";
		}
	}

	private static Method getMethod(Class<?> cls) {
		for (Method m : cls.getMethods())
			if (m.getName().equals("test")) return m;

		throw new IllegalArgumentException();
	}

	private static void checkNoArgs(MethodDeclaration decl) {
		decl.validateUnnamedEnvArgs();
		decl.validateEnvArgs(NO_OPTIONALS);
	}

	private static void checkTargetOnly(MethodDeclaration decl) {
		decl.validateUnnamedEnvArgs(B.class);
		decl.validateEnvArgs(NO_OPTIONALS);
	}

	private static MethodDeclaration createMethodDecl(Class<?> cls) {
		final Method m = getMethod(cls);
		return new MethodDeclaration(cls, m, m.getAnnotation(ScriptCallable.class), "test");
	}

	private static Map<String, Class<?>> singleArg(String name, Class<?> cls) {
		return ImmutableMap.<String, Class<?>> of(name, cls);
	}

	private static Map<String, Class<?>> twoArgs(String name1, Class<?> cls1, String name2, Class<?> cls2) {
		return ImmutableMap.<String, Class<?>> of(name1, cls1, name2, cls2);
	}

	public static class BaseTargetOnly {
		@ScriptCallable(returnTypes = ReturnType.BOOLEAN)
		public boolean test(A target, @Env("env1") A e) {
			return true;
		}
	}

	@Test
	public void testBaseTargetOnly() {
		MethodDeclaration decl = createMethodDecl(BaseTargetOnly.class);
		decl.validateUnnamedEnvArgs(B.class);
		decl.validateEnvArgs(singleArg("env1", B.class));
	}

	public static class TargetOnly {
		@Alias({ "aliasA", "aliasB" })
		@ScriptCallable(returnTypes = ReturnType.BOOLEAN)
		public boolean test(B target) {
			return true;
		}
	}

	@Test
	public void testTargetOnly() {
		MethodDeclaration decl = createMethodDecl(TargetOnly.class);
		Assert.assertEquals(Sets.newHashSet(decl.getNames()), Sets.newHashSet("test", "aliasA", "aliasB"));
		checkTargetOnly(decl);
	}

	public static class SingleLuaArg {
		@ScriptCallable(returnTypes = ReturnType.BOOLEAN)
		public boolean test(B target, @Arg(name = "a") int a) {
			return true;
		}
	}

	@Test
	public void testSingleLuaArg() {
		MethodDeclaration decl = createMethodDecl(SingleLuaArg.class);
		checkTargetOnly(decl);
	}

	public static class SingleEnv {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(B target, @Env("env1") D access) {
			return access.test();
		}
	}

	@Test
	public void testSingleEnv() {
		MethodDeclaration decl = createMethodDecl(SingleEnv.class);
		decl.validateUnnamedEnvArgs(B.class);
		decl.validateEnvArgs(singleArg("env1", D.class));
	}

	@Test(expected = Exception.class)
	public void testMissingEnvName() {
		MethodDeclaration decl = createMethodDecl(SingleEnv.class);
		decl.validateUnnamedEnvArgs(B.class);
		decl.validateEnvArgs(singleArg("env2", D.class));
	}

	@Test(expected = Exception.class)
	public void testMissingEnvType() {
		MethodDeclaration decl = createMethodDecl(SingleEnv.class);
		decl.validateUnnamedEnvArgs(B.class);
		decl.validateEnvArgs(singleArg("env1", B.class));
	}

	@Test(expected = Exception.class)
	public void testMissingPositioned() {
		MethodDeclaration decl = createMethodDecl(SingleEnv.class);
		decl.validateUnnamedEnvArgs(B.class, B.class);
	}

	@Test(expected = Exception.class)
	public void testInvalidTypePositioned() {
		MethodDeclaration decl = createMethodDecl(SingleEnv.class);
		decl.validateUnnamedEnvArgs(D.class);
	}

	public static class Empty {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test() {
			return "oops";
		}
	}

	@Test
	public void testEmpty() {
		MethodDeclaration decl = createMethodDecl(Empty.class);
		checkNoArgs(decl);
	}

	public static class TwoEnvOnly {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(@Env("env1") D access, @Env("target") Object target) {
			return access.toString();
		}
	}

	@Test
	public void testTwoEnvOnly() {
		MethodDeclaration decl = createMethodDecl(TwoEnvOnly.class);
		decl.validateUnnamedEnvArgs();
		decl.validateEnvArgs(twoArgs("env1", D.class, "target", Object.class));
	}

	public static class SingleEnvOnly {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(@Env("env1") D access) {
			return access.toString();
		}
	}

	@Test
	public void testSingleEnvOnly() {
		MethodDeclaration decl = createMethodDecl(SingleEnvOnly.class);
		decl.validateUnnamedEnvArgs();
		decl.validateEnvArgs(singleArg("env1", D.class));
	}

	public static class SingleLuaOnly {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(@Arg(name = "a") int a) {
			return "" + a;
		}
	}

	@Test
	public void testSingleLuaOnly() {
		MethodDeclaration decl = createMethodDecl(SingleLuaOnly.class);
		checkNoArgs(decl);
	}

	public static class SingleOptionalLuaOnly {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(@Optionals @Arg(name = "a") Integer a) {
			return "" + a;
		}
	}

	@Test
	public void testSingleOptionalLuaOnly() {
		MethodDeclaration decl = createMethodDecl(SingleOptionalLuaOnly.class);
		checkNoArgs(decl);
	}

	public static class VarargLuaStart {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(@Arg(name = "a") int... a) {
			return Arrays.toString(a);
		}
	}

	@Test
	public void testVarargLuaStart() {
		MethodDeclaration decl = createMethodDecl(VarargLuaStart.class);
		checkNoArgs(decl);
	}

	public static class OptionalVararg {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(@Optionals @Arg(name = "a") Integer... a) {
			return Arrays.toString(a);
		}
	}

	@Test
	public void testOptionalVararg() {
		MethodDeclaration decl = createMethodDecl(OptionalVararg.class);
		checkNoArgs(decl);
	}

	public static class EnvLua {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(B target, @Env("env1") D access, @Arg(name = "a") Integer a) {
			return access.toString();
		}
	}

	@Test
	public void testEnvLua() {
		MethodDeclaration decl = createMethodDecl(EnvLua.class);
		decl.validateUnnamedEnvArgs(B.class);
		decl.validateEnvArgs(singleArg("env1", D.class));
	}

	public static class FullOptional {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(B target, @Arg(name = "a") int a, @Optionals @Arg(name = "b") String b) {
			return "A";
		}
	}

	@Test
	public void testFullOptional() {
		MethodDeclaration decl = createMethodDecl(FullOptional.class);
		checkTargetOnly(decl);
	}

	public static class SingleOptional {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(B target, @Optionals @Arg(name = "a") String b) {
			return "A";
		}
	}

	@Test
	public void testSingleOptional() {
		MethodDeclaration decl = createMethodDecl(SingleOptional.class);
		checkTargetOnly(decl);
	}

	public static class Vararg {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(B target, @Arg(name = "a") int... a) {
			return Arrays.toString(a);
		}
	}

	@Test
	public void testVararg() {
		MethodDeclaration decl = createMethodDecl(Vararg.class);
		checkTargetOnly(decl);
	}

	public static class Everything {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(B target, @Env("env1") D access, @Arg(name = "a") int a, @Optionals @Arg(name = "b") String b, @Arg(name = "var") Integer... v) {
			return access.test();
		}
	}

	@Test
	public void testEverything() {
		MethodDeclaration decl = createMethodDecl(Everything.class);
		decl.validateUnnamedEnvArgs(B.class);
		decl.validateEnvArgs(singleArg("env1", D.class));
	}

	public static class GenericBase<T, E, P> {
		@ScriptCallable(returnTypes = ReturnType.STRING)
		public String test(T target, @Env("env1") E access, @Arg(name = "a") P a) {
			return String.valueOf(a);
		}
	}

	public static class GenericDerrived extends GenericBase<B, D, Float> {}

	@Test
	public void testGeneric() {
		MethodDeclaration decl = createMethodDecl(GenericDerrived.class);
		decl.validateUnnamedEnvArgs(B.class);
		decl.validateEnvArgs(singleArg("env1", D.class));
	}

	public static class MultiDirect {
		@ScriptCallable(returnTypes = { ReturnType.NUMBER, ReturnType.NUMBER })
		public IMultiReturn test(B target, @Arg(name = "a") int a) {
			return MultiReturn.wrap(a, a + 1);
		}
	}

	@Test
	public void testMultiDirect() {
		createMethodDecl(MultiDirect.class);
	}

	public static class MultiArray {
		@MultipleReturn
		@ScriptCallable(returnTypes = { ReturnType.NUMBER, ReturnType.NUMBER })
		public int[] test(B target, @Arg(name = "a") int a) {
			return new int[] { a, a + 1 };
		}
	}

	@Test
	public void testMultiArray() {
		createMethodDecl(MultiArray.class);
	}

	public static class NonMultiArray {
		@ScriptCallable(returnTypes = ReturnType.TABLE)
		public int[] test(B target, @Arg(name = "a") int a) {
			return new int[] { a, a + 1 };
		}
	}

	@Test
	public void testNonMultiArray() {
		createMethodDecl(NonMultiArray.class);
	}

	public static class MultiCollection {
		@MultipleReturn
		@ScriptCallable(returnTypes = { ReturnType.NUMBER, ReturnType.NUMBER })
		public List<Integer> test(B target, @Arg(name = "a") int a) {
			return Lists.newArrayList(a, a + 1);
		}
	}

	@Test
	public void testMultiCollection() {
		createMethodDecl(MultiCollection.class);
	}

	public static class NonMultiCollection {
		@ScriptCallable(returnTypes = ReturnType.TABLE)
		public Collection<Integer> test(B target, @Arg(name = "a") int a) {
			return Sets.newHashSet(a, a + a);
		}
	}

	@Test
	public void testNonMultiCollection() {
		createMethodDecl(NonMultiCollection.class);
	}

	public static class MultiCollectionVoid {
		@MultipleReturn
		@ScriptCallable(returnTypes = {})
		public Collection<Integer> test(B target, @Arg(name = "a") int a) {
			return Sets.newHashSet(a, a + a);
		}
	}

	@Test(expected = Exception.class)
	public void testMultiCollectionVoid() {
		createMethodDecl(MultiCollectionVoid.class);
	}

	public static class MultiReturnVoid {
		@ScriptCallable(returnTypes = {})
		public IMultiReturn test(B target, @Arg(name = "a") int a) {
			return null;
		}
	}

	@Test(expected = Exception.class)
	public void testMultiReturnVoid() {
		createMethodDecl(MultiReturnVoid.class);
	}

	public static class TwoUnnamed {
		@ScriptCallable(returnTypes = ReturnType.BOOLEAN)
		public boolean test(B target, Object target2) {
			return true;
		}
	}

	@Test
	public void testTwoUnnamed() {
		createMethodDecl(TwoUnnamed.class);
	}

	public static class UnnamedAfterLua {
		@ScriptCallable(returnTypes = ReturnType.BOOLEAN)
		public boolean test(@Arg(name = "foo") String arg, B target) {
			return true;
		}
	}

	@Test(expected = MethodDeclaration.ArgumentDefinitionException.class)
	public void testUnnamedAfterLua() {
		createMethodDecl(UnnamedAfterLua.class);
	}

	public static class EnvAfterLua {
		@ScriptCallable(returnTypes = ReturnType.BOOLEAN)
		public boolean test(@Arg(name = "foo") String arg, @Env("env1") D target) {
			return true;
		}
	}

	@Test(expected = MethodDeclaration.ArgumentDefinitionException.class)
	public void testEnvAfterLua() {
		createMethodDecl(EnvAfterLua.class);
	}

	public static class UnamedAfterEnv {
		@ScriptCallable(returnTypes = ReturnType.BOOLEAN)
		public boolean test(@Env("env1") D e1, B target) {
			return true;
		}
	}

	@Test(expected = MethodDeclaration.ArgumentDefinitionException.class)
	public void testUnamedAfterEnv() {
		createMethodDecl(UnamedAfterEnv.class);
	}

	public static class OptionalUnnnamed {
		@ScriptCallable(returnTypes = ReturnType.BOOLEAN)
		public boolean test(@Optionals B target) {
			return true;
		}
	}

	@Test(expected = MethodDeclaration.ArgumentDefinitionException.class)
	public void testOptionalUnnnamed() {
		createMethodDecl(OptionalUnnnamed.class);
	}

	public static class OptionalEnv {
		@ScriptCallable(returnTypes = ReturnType.BOOLEAN)
		public boolean test(@Optionals @Env("target") B target) {
			return true;
		}
	}

	@Test(expected = MethodDeclaration.ArgumentDefinitionException.class)
	public void testOptionalEnv() {
		createMethodDecl(OptionalEnv.class);
	}

	public static class SameNamedEnv {
		@ScriptCallable(returnTypes = ReturnType.BOOLEAN)
		public boolean test(@Env("target") B target, @Env("target") B target2) {
			return true;
		}
	}

	@Test(expected = MethodDeclaration.ArgumentDefinitionException.class)
	public void testSameNamedEnv() {
		createMethodDecl(SameNamedEnv.class);
	}

}
