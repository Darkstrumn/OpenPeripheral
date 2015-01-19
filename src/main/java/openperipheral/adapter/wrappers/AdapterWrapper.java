package openperipheral.adapter.wrappers;

import java.lang.reflect.Method;
import java.util.List;

import openmods.Log;
import openperipheral.adapter.AsyncChecker;
import openperipheral.adapter.IMethodExecutor;
import openperipheral.adapter.method.MethodDeclaration;
import openperipheral.api.LuaCallable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public abstract class AdapterWrapper {

	public static class MethodWrapException extends RuntimeException {
		private static final long serialVersionUID = -5116134133615320058L;

		public MethodWrapException(Method method, Throwable cause) {
			super(String.format("Failed to wrap method '%s'", method), cause);
		}
	}

	protected final List<IMethodExecutor> methods;
	protected final Class<?> targetClass;
	protected final Class<?> adapterClass;
	protected final String source;
	protected final AsyncChecker asyncChecker;

	protected AdapterWrapper(Class<?> adapterClass, Class<?> targetClass, String source) {
		this.adapterClass = adapterClass;
		this.targetClass = targetClass;
		this.source = source;
		this.asyncChecker = new AsyncChecker(adapterClass);
		this.methods = ImmutableList.copyOf(buildMethodList());
	}

	public String source() {
		return source;
	}

	public List<IMethodExecutor> getMethods() {
		return methods;
	}

	public Class<?> getAdapterClass() {
		return adapterClass;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public abstract boolean canUse(Class<?> cls);

	public abstract String describe();

	public abstract IMethodExecutor createExecutor(Method method, MethodDeclaration decl);

	protected abstract void verifyArguments(MethodDeclaration decl);

	protected List<IMethodExecutor> buildMethodList() {
		List<IMethodExecutor> result = Lists.newArrayList();

		Method[] clsMethods;
		try {
			clsMethods = adapterClass.getDeclaredMethods();
		} catch (Throwable t) {
			// Trust no one. We got report about ClassNotFound from this place
			Log.severe(t, "Can't get adapter %s methods (possible sideness fail), bailing out", adapterClass);
			return result;
		}

		for (Method method : clsMethods) {
			try {
				LuaCallable callableAnn = method.getAnnotation(LuaCallable.class);
				if (callableAnn == null) continue;

				final MethodDeclaration decl = new MethodDeclaration(method, callableAnn, source);
				verifyArguments(decl);
				IMethodExecutor exec = createExecutor(method, decl);
				result.add(exec);
			} catch (Throwable e) {
				throw new MethodWrapException(method, e);
			}
		}

		return result;
	}
}