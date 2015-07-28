package openperipheral.interfaces.cc;

import openmods.Log;
import openperipheral.adapter.IMethodCall;
import openperipheral.api.Constants;
import openperipheral.api.architecture.IArchitectureAccess;
import openperipheral.api.converter.IConverter;
import openperipheral.api.helpers.Index;
import openperipheral.converter.TypeConvertersProvider;
import openperipheral.interfaces.cc.wrappers.LuaObjectWrapper;

import org.apache.logging.log4j.Level;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class ComputerCraftEnv {

	public static IArchitectureAccess createAccess(final IComputerAccess access) {
		return new IArchitectureAccess() {
			@Override
			public String architecture() {
				return Constants.ARCH_COMPUTER_CRAFT;
			}

			@Override
			public String callerName() {
				return Integer.toString(access.getID());
			}

			@Override
			public String peripheralName() {
				return access.getAttachmentName();
			}

			@Override
			public boolean signal(String name, Object... args) {
				try {
					access.queueEvent(name, args);
					return true;
				} catch (Exception e) {
					Log.log(Level.DEBUG, e, "Failed to send signal: %s", name);
				}
				return false;
			}

			@Override
			public Object wrapObject(Object target) {
				return LuaObjectWrapper.wrap(target);
			}

			@Override
			public boolean canSignal() {
				try {
					// this should throw if peripheral isn't attached
					access.getAttachmentName();
					return true;
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public Index createIndex(int value) {
				return new Index(value, 1);
			}
		};
	}

	public static IMethodCall addCommonArgs(IMethodCall call, ILuaContext context) {
		final IConverter converter = TypeConvertersProvider.INSTANCE.getConverter(Constants.ARCH_COMPUTER_CRAFT);
		return call
				.setEnv(Constants.ARG_CONVERTER, converter)
				.setEnv(Constants.ARG_CONTEXT, context);
	}

	public static IMethodCall addPeripheralArgs(IMethodCall call, IComputerAccess access, ILuaContext context) {
		final IArchitectureAccess wrappedAccess = createAccess(access);
		return addCommonArgs(call, context)
				.setEnv(Constants.ARG_ACCESS, wrappedAccess)
				.setEnv(Constants.ARG_COMPUTER, access);
	}
}
