package openperipheral.adapter.peripheral;

import java.util.Arrays;
import java.util.logging.Level;

import openmods.Log;
import openperipheral.adapter.AdaptedClass;
import openperipheral.api.IAttachable;
import openperipheral.util.EqualsUtil;
import openperipheral.util.PeripheralUtils;
import openperipheral.util.ResourceMount;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;


public class HostedPeripheralBase<T> implements IPeripheral {

	private static final String MOUNT_NAME = "openp";
	private static final IMount MOUNT = new ResourceMount();

	protected final String type;
	protected final T targetObject;
	protected final AdaptedClass<IPeripheralMethodExecutor> wrapped;

	protected HostedPeripheralBase(AdaptedClass<IPeripheralMethodExecutor> wrapper, T targetObject) {
		this.targetObject = targetObject;
		this.type = PeripheralUtils.getNameForTarget(targetObject);
		this.wrapped = wrapper;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String[] getMethodNames() {
		return wrapped.methodNames;
	}

	@Override
	public Object[] callMethod(final IComputerAccess computer, ILuaContext context, int index, Object[] arguments) throws Exception {
		try {
			IPeripheralMethodExecutor executor = wrapped.getMethod(index);
			return executor.execute(computer, context, targetObject, arguments);
		} catch (Exception e) {
			Log.log(Level.FINE, e, "Error during method %d execution on peripheral %s, args: %s", index, type, Arrays.toString(arguments));
			throw e;
		}
	}

	@Override
	public void attach(IComputerAccess computer) {
		computer.mount(MOUNT_NAME, HostedPeripheralBase.MOUNT);
		if (targetObject instanceof IAttachable) ((IAttachable)targetObject).addComputer(computer);
	}

	@Override
	public void detach(IComputerAccess computer) {
		if (targetObject instanceof IAttachable) ((IAttachable)targetObject).removeComputer(computer);
	}

	@Override
	public boolean equals(IPeripheral other) {
		if(!(other instanceof HostedPeripheralBase)){
			return false;
		}
		HostedPeripheralBase<?> other_hpb=(HostedPeripheralBase<?>)other;
		return EqualsUtil.equals(this.wrapped, other_hpb.wrapped) && EqualsUtil.equals(this.targetObject, other_hpb.targetObject);

	}


}
